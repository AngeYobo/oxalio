-- 001_terne_core_v2_audit_grade.sql (PostgreSQL)
-- Objectifs:
-- - Soft-delete terminal (pas de suppression destructive des preuves)
-- - Corrélation commands/events (correlation_id + request_id)
-- - Trigger updated_at (DB-side)
-- - Index ciblés: latest location, commands queued/actives, lookups audit

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- 1--) TERMINALS (soft-delete)
-- =========================
CREATE TABLE IF NOT EXISTS terne_terminals (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  pos_id          UUID NULL,

  status          TEXT NOT NULL CHECK (status IN ('ENROLLED','ACTIVE','SUSPENDED','RETIRED')),

  serial_number   VARCHAR(64) NOT NULL,
  imei            VARCHAR(32) NULL,

  manufacturer    VARCHAR(80) NOT NULL,
  model           VARCHAR(80) NOT NULL,
  os_version      VARCHAR(32) NOT NULL,
  app_version     VARCHAR(32) NULL,

  mdm_provider    VARCHAR(50) NULL,
  mdm_device_id   VARCHAR(100) NULL,

  tags            TEXT[] NULL,

  last_seen_at    TIMESTAMPTZ NULL,

  -- Soft-delete / lifecycle audit
  retired_at      TIMESTAMPTZ NULL,
  deleted_at      TIMESTAMPTZ NULL,     -- soft-delete marker (do not cascade-delete children)
  deleted_by      VARCHAR(120) NULL,    -- actor (support/admin/service)
  delete_reason   VARCHAR(300) NULL,

  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Remplacer le DO $$ par un CREATE UNIQUE INDEX IF NOT EXISTS pour éviter les problèmes de verrouillage dans les environnements à haute concurrence (ex: prod)
CREATE UNIQUE INDEX IF NOT EXISTS ux_terne_terminals_serial
  ON terne_terminals(serial_number);


-- Unique IMEI only if present
CREATE UNIQUE INDEX IF NOT EXISTS ux_terne_terminals_imei
  ON terne_terminals(imei)
  WHERE imei IS NOT NULL;

-- Operational / audit indexes
CREATE INDEX IF NOT EXISTS ix_terne_terminals_tenant
  ON terne_terminals(tenant_id);

CREATE INDEX IF NOT EXISTS ix_terne_terminals_pos
  ON terne_terminals(pos_id);

CREATE INDEX IF NOT EXISTS ix_terne_terminals_status
  ON terne_terminals(status);

CREATE INDEX IF NOT EXISTS ix_terne_terminals_mdm_device
  ON terne_terminals(mdm_provider, mdm_device_id);

-- Only active (non-deleted) terminals lookup fast
CREATE INDEX IF NOT EXISTS ix_terne_terminals_not_deleted
  ON terne_terminals(id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS ix_terne_terminals_serial_not_deleted
  ON terne_terminals(serial_number)
  WHERE deleted_at IS NULL;

-- =========================
-- 2--) LOCATIONS (no cascade)
-- =========================
CREATE TABLE IF NOT EXISTS terne_terminal_locations (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  terminal_id      UUID NOT NULL REFERENCES terne_terminals(id),

  captured_at      TIMESTAMPTZ NOT NULL,
  source           TEXT NOT NULL CHECK (source IN ('MDM','DEVICE_AGENT','MANUAL')),

  latitude         DOUBLE PRECISION NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
  longitude        DOUBLE PRECISION NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
  accuracy_meters  DOUBLE PRECISION NULL CHECK (accuracy_meters IS NULL OR accuracy_meters >= 0),
  provider         VARCHAR(50) NULL,

  -- Corrélation (optionnel mais utile pour audit)
  correlation_id   UUID NULL,
  request_id       UUID NULL,

  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Latest + history
CREATE INDEX IF NOT EXISTS ix_terne_locations_terminal_time
  ON terne_terminal_locations(terminal_id, captured_at DESC);

-- Targeted: latest location very fast (index-only scans possible)
CREATE INDEX IF NOT EXISTS ix_terne_locations_latest
  ON terne_terminal_locations(terminal_id, captured_at DESC)
  INCLUDE (latitude, longitude, accuracy_meters, source, provider);

-- Corrélation
CREATE INDEX IF NOT EXISTS ix_terne_locations_correlation
  ON terne_terminal_locations(correlation_id)
  WHERE correlation_id IS NOT NULL;

-- =========================
-- 3--) EVENTS (no cascade)
-- =========================
CREATE TABLE IF NOT EXISTS terne_terminal_events (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  terminal_id   UUID NOT NULL REFERENCES terne_terminals(id),

  event_type    TEXT NOT NULL CHECK (event_type IN ('HEARTBEAT','APP_EVENT','MDM_EVENT')),
  captured_at   TIMESTAMPTZ NOT NULL,

  ip_address    VARCHAR(64) NULL,
  network_type  TEXT NULL CHECK (network_type IN ('WIFI','CELLULAR_2G','CELLULAR_3G','CELLULAR_4G','CELLULAR_5G','ETHERNET','UNKNOWN')),
  battery_pct   INTEGER NULL CHECK (battery_pct IS NULL OR (battery_pct >= 0 AND battery_pct <= 100)),
  charging      BOOLEAN NULL,

  app_version   VARCHAR(32) NULL,
  os_version    VARCHAR(32) NULL,
  mdm_compliant BOOLEAN NULL,

  -- Corrélation + acteur
  correlation_id UUID NULL,
  request_id     UUID NULL,
  actor          VARCHAR(120) NULL, -- e.g. "SOTI", "DEVICE_AGENT", "SUPPORT", "SYSTEM"

  payload       JSONB NULL,

  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_terne_events_terminal_time
  ON terne_terminal_events(terminal_id, captured_at DESC);

CREATE INDEX IF NOT EXISTS ix_terne_events_type_time
  ON terne_terminal_events(event_type, captured_at DESC);

CREATE INDEX IF NOT EXISTS ix_terne_events_correlation
  ON terne_terminal_events(correlation_id)
  WHERE correlation_id IS NOT NULL;

-- =========================
-- 4--) COMMANDS (no cascade)
-- =========================
CREATE TABLE IF NOT EXISTS terne_terminal_commands (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  terminal_id     UUID NOT NULL REFERENCES terne_terminals(id),

  command_type    TEXT NOT NULL CHECK (command_type IN ('LOCK','WIPE','RESTART_DEVICE','RESTART_APP','SYNC_CONFIG','PUSH_APP_UPDATE')),
  status          TEXT NOT NULL CHECK (status IN ('QUEUED','ACKED','RUNNING','SUCCEEDED','FAILED','CANCELED')),

  payload         JSONB NULL,

  requested_by    VARCHAR(120) NULL,
  requested_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

  -- Corrélation
  correlation_id  UUID NULL,
  request_id      UUID NULL,

  acknowledged_at TIMESTAMPTZ NULL,
  completed_at    TIMESTAMPTZ NULL,

  error_code      VARCHAR(80) NULL,
  error_message   VARCHAR(1000) NULL,

  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Existing index (good for per-terminal queue/history)
CREATE INDEX IF NOT EXISTS ix_terne_commands_terminal_status
  ON terne_terminal_commands(terminal_id, status, created_at DESC);

-- Targeted: quickly pull queued/active commands (worker/device polling)
CREATE INDEX IF NOT EXISTS ix_terne_commands_queued
  ON terne_terminal_commands(terminal_id, created_at ASC)
  WHERE status = 'QUEUED';

CREATE INDEX IF NOT EXISTS ix_terne_commands_active
  ON terne_terminal_commands(terminal_id, created_at ASC)
  WHERE status IN ('QUEUED','ACKED','RUNNING');

-- Corrélation
CREATE INDEX IF NOT EXISTS ix_terne_commands_correlation
  ON terne_terminal_commands(correlation_id)
  WHERE correlation_id IS NOT NULL;

-- =========================
-- 5--) TRIGGERS updated_at
-- =========================
CREATE OR REPLACE FUNCTION terne_set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- terminals updated_at
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_terne_terminals_set_updated_at') THEN
    CREATE TRIGGER trg_terne_terminals_set_updated_at
    BEFORE UPDATE ON terne_terminals
    FOR EACH ROW
    EXECUTE FUNCTION terne_set_updated_at();
  END IF;
END$$;

-- commands updated_at
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_terne_commands_set_updated_at') THEN
    CREATE TRIGGER trg_terne_commands_set_updated_at
    BEFORE UPDATE ON terne_terminal_commands
    FOR EACH ROW
    EXECUTE FUNCTION terne_set_updated_at();
  END IF;
END$$;

-- =========================
-- 6--) -- (OPTIONNEL) Guardrails soft-delete
-- =========================
-- 6.1 Prevent hard delete of terminals in prod -- (optional policy)
-- You can enforce this via RBAC, or uncomment below to forbid DELETE at DB layer.
-- CREATE OR REPLACE FUNCTION terne_forbid_delete()
-- RETURNS trigger AS $$
-- BEGIN
--   RAISE EXCEPTION 'Hard delete is forbidden for audit reasons. Use deleted_at soft-delete.';
-- END;
-- $$ LANGUAGE plpgsql;
--
-- DO $$
-- BEGIN
--   IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_terne_terminals_forbid_delete') THEN
--     CREATE TRIGGER trg_terne_terminals_forbid_delete
--     BEFORE DELETE ON terne_terminals
--     FOR EACH ROW
--     EXECUTE FUNCTION terne_forbid_delete();
--   END IF;
-- END$$;
