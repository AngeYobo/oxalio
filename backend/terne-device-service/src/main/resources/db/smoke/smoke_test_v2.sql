-- smoke_test_v2.sql (PostgreSQL) — idempotent
\echo '=== smoke_test_v2: start ==='
\set ON_ERROR_STOP on

BEGIN;

-- =========================
-- 0) Context / constants
-- =========================
-- Correlation/request IDs fresh each run
SELECT gen_random_uuid() AS correlation_id, gen_random_uuid() AS request_id \gset

-- =========================
-- 1) Upsert terminal (idempotent)
-- =========================
-- Create terminal if missing, otherwise reset it
INSERT INTO terne_terminals (
  tenant_id, status, serial_number, manufacturer, model, os_version
)
SELECT
  gen_random_uuid(), 'ACTIVE', 'SN-SMOKE-001', 'PAX', 'A920', 'Android-13'
WHERE NOT EXISTS (
  SELECT 1 FROM terne_terminals WHERE serial_number = 'SN-SMOKE-001'
);

-- Ensure it is not soft-deleted and ACTIVE
UPDATE terne_terminals
SET
  status = 'ACTIVE',
  deleted_at = NULL,
  deleted_by = NULL,
  delete_reason = NULL
WHERE serial_number = 'SN-SMOKE-001';

-- Capture terminal_id into psql var :terminal_id
SELECT id AS terminal_id
FROM terne_terminals
WHERE serial_number = 'SN-SMOKE-001'
\gset

\echo 'Terminal id:'
SELECT :'terminal_id'::uuid AS terminal_id;

-- =========================
-- 2) updated_at trigger check
-- =========================
\echo '--- updated_at trigger: terminals ---'
SELECT serial_number, app_version, created_at, updated_at
FROM terne_terminals
WHERE id = :'terminal_id'::uuid;

-- Update should bump updated_at via trigger
UPDATE terne_terminals
SET app_version = '1.0.2'
WHERE id = :'terminal_id'::uuid;

SELECT serial_number, app_version, created_at, updated_at
FROM terne_terminals
WHERE id = :'terminal_id'::uuid;

-- =========================
-- 3) soft-delete check + partial index usage
-- =========================
\echo '--- soft-delete + ix_terne_terminals_serial_not_deleted ---'

UPDATE terne_terminals
SET deleted_at = now(), deleted_by='support', delete_reason='smoke-test'
WHERE id = :'terminal_id'::uuid;

-- Should use ix_terne_terminals_serial_not_deleted and return 0 row
EXPLAIN (ANALYZE, BUFFERS)
SELECT id
FROM terne_terminals
WHERE serial_number='SN-SMOKE-001' AND deleted_at IS NULL;

-- Restore (idempotent)
UPDATE terne_terminals
SET deleted_at=NULL, deleted_by=NULL, delete_reason=NULL
WHERE id = :'terminal_id'::uuid;

-- =========================
-- 4) correlation on locations + "latest location" index
-- =========================
\echo '--- correlation + latest location index ---'

-- Clean previous test data for the same correlation (idempotent)
DELETE FROM terne_terminal_locations WHERE correlation_id = :'correlation_id'::uuid;

-- Insert 2 locations (older + newer)
INSERT INTO terne_terminal_locations (
  terminal_id, captured_at, source,
  latitude, longitude, accuracy_meters, provider,
  correlation_id, request_id
)
VALUES
  (:'terminal_id'::uuid, now() - interval '2 minutes', 'DEVICE_AGENT',
   5.3480, -4.0120, 12.0, 'gps',
   :'correlation_id'::uuid, :'request_id'::uuid),
  (:'terminal_id'::uuid, now() - interval '10 seconds', 'DEVICE_AGENT',
   5.3490, -4.0110, 8.0, 'gps',
   :'correlation_id'::uuid, :'request_id'::uuid);

-- Verify we can retrieve "latest" fast (should hit ix_terne_locations_latest)
EXPLAIN (ANALYZE, BUFFERS)
SELECT terminal_id, captured_at, latitude, longitude, accuracy_meters, source, provider
FROM terne_terminal_locations
WHERE terminal_id = :'terminal_id'::uuid
ORDER BY captured_at DESC
LIMIT 1;

-- Verify correlation index works
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, terminal_id, captured_at
FROM terne_terminal_locations
WHERE correlation_id = :'correlation_id'::uuid;

-- =========================
-- 5) queued/active commands indexes + correlation
-- =========================
\echo '--- queued/active commands indexes + correlation ---'

-- Clean previous test data for the same correlation (idempotent)
DELETE FROM terne_terminal_commands WHERE correlation_id = :'correlation_id'::uuid;

-- Insert commands (QUEUED + ACKED)
INSERT INTO terne_terminal_commands (
  terminal_id, command_type, status, payload, requested_by,
  correlation_id, request_id
)
VALUES
  (:'terminal_id'::uuid, 'SYNC_CONFIG', 'QUEUED', '{"scope":"all"}'::jsonb, 'smoke-test',
   :'correlation_id'::uuid, :'request_id'::uuid),
  (:'terminal_id'::uuid, 'RESTART_APP', 'ACKED', '{"app":"terne"}'::jsonb, 'smoke-test',
   :'correlation_id'::uuid, :'request_id'::uuid);

-- QUEUED query (should use ix_terne_commands_queued)
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, command_type, status, created_at
FROM terne_terminal_commands
WHERE terminal_id = :'terminal_id'::uuid
  AND status = 'QUEUED'
ORDER BY created_at ASC
LIMIT 10;

-- ACTIVE query (should use ix_terne_commands_active)
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, command_type, status, created_at
FROM terne_terminal_commands
WHERE terminal_id = :'terminal_id'::uuid
  AND status IN ('QUEUED','ACKED','RUNNING')
ORDER BY created_at ASC
LIMIT 10;

-- Correlation query (should use ix_terne_commands_correlation)
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, created_at
FROM terne_terminal_commands
WHERE correlation_id = :'correlation_id'::uuid;

COMMIT;

\echo '=== smoke_test_v2: done ==='
