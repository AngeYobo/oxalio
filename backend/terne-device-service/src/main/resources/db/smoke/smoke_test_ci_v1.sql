\set ON_ERROR_STOP on
\pset pager off

-- ============================================
-- smoke_test_ci_v1.sql (PostgreSQL)
-- - unique serial_number (timestamp-based)
-- - rollback cleanup
-- - clear OK/FAIL exit (via ON_ERROR_STOP)
-- ============================================

BEGIN;

-- ---------- Variables ----------
-- Unique serial: SN-CI-YYYYMMDDHHMMSSMS-PID
SELECT format(
  'SN-CI-%s-%s',
  to_char(clock_timestamp(), 'YYYYMMDDHH24MISSMS'),
  pg_backend_pid()
) AS serial \gset

-- Correlation UUID for correlation tests
SELECT gen_random_uuid() AS corr_id \gset

\echo === smoke_test_ci_v1: start ===
\echo serial_number=:serial
\echo correlation_id=:corr_id

-- ---------- 0) Preconditions ----------
-- pgcrypto is needed for gen_random_uuid()
-- If your migrations already created the extension, this is harmless.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------- 1) Insert terminal ----------
INSERT INTO terne_terminals (
  tenant_id, status, serial_number, manufacturer, model, os_version
)
VALUES (
  gen_random_uuid(), 'ENROLLED', :'serial', 'PAX', 'A920', 'Android-13'
);

-- Ensure terminal exists and capture id
SELECT id AS terminal_id
FROM terne_terminals
WHERE serial_number = :'serial'
\gset

DO $$
BEGIN
  IF :'terminal_id' IS NULL OR length(:'terminal_id') = 0 THEN
    RAISE EXCEPTION 'FAIL: terminal_id not captured';
  END IF;
END$$;

\echo terminal_id=:terminal_id

-- ---------- 2) Trigger updated_at ----------
-- We expect updated_at to change after update.
-- Ensure some time gap (optional but makes it robust)
SELECT pg_sleep(0.05);

WITH before_row AS (
  SELECT created_at, updated_at
  FROM terne_terminals
  WHERE id = :'terminal_id'::uuid
),
upd AS (
  UPDATE terne_terminals
  SET app_version = 'ci-1.0.1'
  WHERE id = :'terminal_id'::uuid
  RETURNING created_at, updated_at
)
SELECT
  (SELECT created_at FROM before_row) AS created_before,
  (SELECT updated_at FROM before_row) AS updated_before,
  (SELECT created_at FROM upd)        AS created_after,
  (SELECT updated_at FROM upd)        AS updated_after
\gset

DO $$
BEGIN
  IF :'created_before' IS NULL OR :'updated_before' IS NULL THEN
    RAISE EXCEPTION 'FAIL: could not read before timestamps';
  END IF;

  -- created_at must not change
  IF :'created_before' <> :'created_after' THEN
    RAISE EXCEPTION 'FAIL: created_at changed (before %, after %)', :'created_before', :'created_after';
  END IF;

  -- updated_at must change (strictly greater)
  IF (:'updated_after')::timestamptz <= (:'updated_before')::timestamptz THEN
    RAISE EXCEPTION 'FAIL: updated_at did not increase (before %, after %)', :'updated_before', :'updated_after';
  END IF;
END$$;

\echo OK: trigger updated_at

-- ---------- 3) Soft-delete + partial index usage ----------
UPDATE terne_terminals
SET deleted_at = now(), deleted_by = 'ci', delete_reason = 'ci-smoke'
WHERE id = :'terminal_id'::uuid;

-- Ensure query excludes deleted rows
DO $$
DECLARE
  cnt int;
BEGIN
  SELECT count(*) INTO cnt
  FROM terne_terminals
  WHERE serial_number = :'serial'
    AND deleted_at IS NULL;

  IF cnt <> 0 THEN
    RAISE EXCEPTION 'FAIL: soft-deleted terminal still visible (cnt=%)', cnt;
  END IF;
END$$;

-- Ensure planner can use the partial index (best-effort assertion)
-- We check the plan text contains the index name.
WITH plan AS (
  SELECT string_agg("QUERY PLAN", E'\n') AS txt
  FROM (
    EXPLAIN (ANALYZE, BUFFERS)
    SELECT id
    FROM terne_terminals
    WHERE serial_number = :'serial'
      AND deleted_at IS NULL
  ) e
)
SELECT txt FROM plan \gset

DO $$
BEGIN
  IF position('ix_terne_terminals_serial_not_deleted' in :'txt') = 0 THEN
    RAISE EXCEPTION 'FAIL: expected index ix_terne_terminals_serial_not_deleted not used/visible in plan. Plan: %', :'txt';
  END IF;
END$$;

\echo OK: soft-delete + index ix_terne_terminals_serial_not_deleted

-- ---------- 4) Correlation + latest location index ----------
-- Insert two locations with same correlation_id; ensure latest is picked.
INSERT INTO terne_terminal_locations (
  terminal_id, captured_at, source, latitude, longitude, accuracy_meters, correlation_id
)
VALUES
  (:'terminal_id'::uuid, now() - interval '10 seconds', 'MDM', 5.3001, -4.0123, 12.0, :'corr_id'::uuid),
  (:'terminal_id'::uuid, now(),                  'MDM', 5.3002, -4.0124, 10.0, :'corr_id'::uuid);

-- Verify correlation index query returns 2 rows
DO $$
DECLARE
  cnt int;
BEGIN
  SELECT count(*) INTO cnt
  FROM terne_terminal_locations
  WHERE correlation_id = :'corr_id'::uuid;

  IF cnt <> 2 THEN
    RAISE EXCEPTION 'FAIL: correlation_id locations count mismatch (cnt=%)', cnt;
  END IF;
END$$;

-- Verify latest location query uses ix_terne_locations_latest (best-effort)
WITH plan AS (
  SELECT string_agg("QUERY PLAN", E'\n') AS txt
  FROM (
    EXPLAIN (ANALYZE, BUFFERS)
    SELECT terminal_id, captured_at, latitude, longitude
    FROM terne_terminal_locations
    WHERE terminal_id = :'terminal_id'::uuid
    ORDER BY captured_at DESC
    LIMIT 1
  ) e
)
SELECT txt FROM plan \gset

DO $$
BEGIN
  IF position('ix_terne_locations_latest' in :'txt') = 0 THEN
    -- acceptable fallback could be ix_terne_locations_terminal_time; but your objective is latest index.
    RAISE EXCEPTION 'FAIL: expected index ix_terne_locations_latest not used/visible in plan. Plan: %', :'txt';
  END IF;
END$$;

\echo OK: correlation + latest location index ix_terne_locations_latest

-- ---------- 5) queued/active commands indexes ----------
-- Insert 3 commands: QUEUED + RUNNING + SUCCEEDED
INSERT INTO terne_terminal_commands (
  terminal_id, command_type, status, payload, requested_by, correlation_id
)
VALUES
  (:'terminal_id'::uuid, 'SYNC_CONFIG',  'QUEUED',    '{"scope":"all"}'::jsonb, 'ci', :'corr_id'::uuid),
  (:'terminal_id'::uuid, 'RESTART_APP',  'RUNNING',   '{}'::jsonb,              'ci', :'corr_id'::uuid),
  (:'terminal_id'::uuid, 'LOCK',         'SUCCEEDED', '{}'::jsonb,              'ci', :'corr_id'::uuid);

-- Ensure queued plan uses ix_terne_commands_queued
WITH plan AS (
  SELECT string_agg("QUERY PLAN", E'\n') AS txt
  FROM (
    EXPLAIN (ANALYZE, BUFFERS)
    SELECT id, status, created_at
    FROM terne_terminal_commands
    WHERE terminal_id = :'terminal_id'::uuid
      AND status = 'QUEUED'
    ORDER BY created_at ASC
    LIMIT 10
  ) e
)
SELECT txt FROM plan \gset

DO $$
BEGIN
  IF position('ix_terne_commands_queued' in :'txt') = 0 THEN
    RAISE EXCEPTION 'FAIL: expected index ix_terne_commands_queued not used/visible in plan. Plan: %', :'txt';
  END IF;
END$$;

-- Ensure active plan uses ix_terne_commands_active
WITH plan AS (
  SELECT string_agg("QUERY PLAN", E'\n') AS txt
  FROM (
    EXPLAIN (ANALYZE, BUFFERS)
    SELECT id, status, created_at
    FROM terne_terminal_commands
    WHERE terminal_id = :'terminal_id'::uuid
      AND status IN ('QUEUED','ACKED','RUNNING')
    ORDER BY created_at ASC
    LIMIT 10
  ) e
)
SELECT txt FROM plan \gset

DO $$
BEGIN
  IF position('ix_terne_commands_active' in :'txt') = 0 THEN
    RAISE EXCEPTION 'FAIL: expected index ix_terne_commands_active not used/visible in plan. Plan: %', :'txt';
  END IF;
END$$;

\echo OK: queued/active commands indexes

-- ---------- End ----------
\echo === smoke_test_ci_v1: OK ===

-- Cleanup: no state left behind
ROLLBACK;
