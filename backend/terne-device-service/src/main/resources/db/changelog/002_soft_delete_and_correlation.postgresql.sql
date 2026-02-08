-- 002_soft_delete_and_correlation.postgresql.sql
-- PostgreSQL migration for:
-- - Soft-delete terminals (audit trail)
-- - Correlation fields across commands/events/locations
-- - DB-side updated_at trigger
-- - Targeted indexes (latest location, queued/active commands)

-- =========================
-- 1) Soft-delete terminals
-- =========================
ALTER TABLE terne_terminals
  ADD COLUMN IF NOT EXISTS retired_at    TIMESTAMPTZ NULL,
  ADD COLUMN IF NOT EXISTS deleted_at    TIMESTAMPTZ NULL,
  ADD COLUMN IF NOT EXISTS deleted_by    VARCHAR(120) NULL,
  ADD COLUMN IF NOT EXISTS delete_reason VARCHAR(300) NULL;

-- Fast lookups for "active" terminals (non-deleted)
CREATE INDEX IF NOT EXISTS ix_terne_terminals_not_deleted
  ON terne_terminals(id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_terne_terminals_serial_not_deleted
  ON terne_terminals(serial_number)
  WHERE deleted_at IS NULL;

-- =========================
-- 2) Correlation IDs
-- =========================
ALTER TABLE terne_terminal_locations
  ADD COLUMN IF NOT EXISTS correlation_id UUID NULL,
  ADD COLUMN IF NOT EXISTS request_id     UUID NULL;

CREATE INDEX IF NOT EXISTS ix_terne_locations_correlation
  ON terne_terminal_locations(correlation_id)
  WHERE correlation_id IS NOT NULL;

-- Latest location optimized (PostgreSQL INCLUDE ok)
CREATE INDEX IF NOT EXISTS ix_terne_locations_latest
  ON terne_terminal_locations(terminal_id, captured_at DESC)
  INCLUDE (latitude, longitude, accuracy_meters, source, provider);

ALTER TABLE terne_terminal_events
  ADD COLUMN IF NOT EXISTS correlation_id UUID NULL,
  ADD COLUMN IF NOT EXISTS request_id     UUID NULL,
  ADD COLUMN IF NOT EXISTS actor          VARCHAR(120) NULL;

CREATE INDEX IF NOT EXISTS ix_terne_events_correlation
  ON terne_terminal_events(correlation_id)
  WHERE correlation_id IS NOT NULL;

ALTER TABLE terne_terminal_commands
  ADD COLUMN IF NOT EXISTS correlation_id UUID NULL,
  ADD COLUMN IF NOT EXISTS request_id     UUID NULL,
  ADD COLUMN IF NOT EXISTS requested_at   TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS ix_terne_commands_correlation
  ON terne_terminal_commands(correlation_id)
  WHERE correlation_id IS NOT NULL;

-- =========================
-- 3) Targeted indexes
-- =========================
-- Quickly fetch queued commands per terminal (device poller / worker)
CREATE INDEX IF NOT EXISTS ix_terne_commands_queued
  ON terne_terminal_commands(terminal_id, created_at ASC)
  WHERE status = 'QUEUED';

-- Quickly fetch active commands
CREATE INDEX IF NOT EXISTS ix_terne_commands_active
  ON terne_terminal_commands(terminal_id, created_at ASC)
  WHERE status IN ('QUEUED','ACKED','RUNNING');

-- =========================
-- 4) updated_at triggers (DB-side)
-- =========================
CREATE OR REPLACE FUNCTION terne_set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_terne_terminals_set_updated_at') THEN
    CREATE TRIGGER trg_terne_terminals_set_updated_at
    BEFORE UPDATE ON terne_terminals
    FOR EACH ROW
    EXECUTE FUNCTION terne_set_updated_at();
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_terne_commands_set_updated_at') THEN
    CREATE TRIGGER trg_terne_commands_set_updated_at
    BEFORE UPDATE ON terne_terminal_commands
    FOR EACH ROW
    EXECUTE FUNCTION terne_set_updated_at();
  END IF;
END$$;
