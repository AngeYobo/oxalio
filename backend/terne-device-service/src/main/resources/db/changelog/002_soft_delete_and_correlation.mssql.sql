/* 002_soft_delete_and_correlation_mssql.sql */

-- 1) SOFT-DELETE TERMINALS
IF COL_LENGTH('terne_terminals', 'retired_at') IS NULL
  ALTER TABLE terne_terminals ADD retired_at DATETIMEOFFSET NULL;

IF COL_LENGTH('terne_terminals', 'deleted_at') IS NULL
  ALTER TABLE terne_terminals ADD deleted_at DATETIMEOFFSET NULL;

IF COL_LENGTH('terne_terminals', 'deleted_by') IS NULL
  ALTER TABLE terne_terminals ADD deleted_by VARCHAR(120) NULL;

IF COL_LENGTH('terne_terminals', 'delete_reason') IS NULL
  ALTER TABLE terne_terminals ADD delete_reason VARCHAR(300) NULL;

-- Partial indexes in MSSQL = filtered indexes
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_terminals_not_deleted' AND object_id = OBJECT_ID('terne_terminals'))
  CREATE INDEX ix_terne_terminals_not_deleted ON terne_terminals(id) WHERE deleted_at IS NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_terminals_serial_not_deleted' AND object_id = OBJECT_ID('terne_terminals'))
  CREATE INDEX ix_terne_terminals_serial_not_deleted ON terne_terminals(serial_number) WHERE deleted_at IS NULL;

-- 2) CORRELATION IDS
IF COL_LENGTH('terne_terminal_locations', 'correlation_id') IS NULL
  ALTER TABLE terne_terminal_locations ADD correlation_id UNIQUEIDENTIFIER NULL;

IF COL_LENGTH('terne_terminal_locations', 'request_id') IS NULL
  ALTER TABLE terne_terminal_locations ADD request_id UNIQUEIDENTIFIER NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_locations_correlation' AND object_id = OBJECT_ID('terne_terminal_locations'))
  CREATE INDEX ix_terne_locations_correlation ON terne_terminal_locations(correlation_id) WHERE correlation_id IS NOT NULL;

-- "latest location" index (INCLUDE supported by MSSQL)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_locations_latest' AND object_id = OBJECT_ID('terne_terminal_locations'))
  CREATE INDEX ix_terne_locations_latest
  ON terne_terminal_locations(terminal_id, captured_at DESC)
  INCLUDE (latitude, longitude, accuracy_meters, source, provider);

-- Events
IF COL_LENGTH('terne_terminal_events', 'correlation_id') IS NULL
  ALTER TABLE terne_terminal_events ADD correlation_id UNIQUEIDENTIFIER NULL;

IF COL_LENGTH('terne_terminal_events', 'request_id') IS NULL
  ALTER TABLE terne_terminal_events ADD request_id UNIQUEIDENTIFIER NULL;

IF COL_LENGTH('terne_terminal_events', 'actor') IS NULL
  ALTER TABLE terne_terminal_events ADD actor VARCHAR(120) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_events_correlation' AND object_id = OBJECT_ID('terne_terminal_events'))
  CREATE INDEX ix_terne_events_correlation ON terne_terminal_events(correlation_id) WHERE correlation_id IS NOT NULL;

-- Commands
IF COL_LENGTH('terne_terminal_commands', 'correlation_id') IS NULL
  ALTER TABLE terne_terminal_commands ADD correlation_id UNIQUEIDENTIFIER NULL;

IF COL_LENGTH('terne_terminal_commands', 'request_id') IS NULL
  ALTER TABLE terne_terminal_commands ADD request_id UNIQUEIDENTIFIER NULL;

IF COL_LENGTH('terne_terminal_commands', 'requested_at') IS NULL
  ALTER TABLE terne_terminal_commands ADD requested_at DATETIMEOFFSET NOT NULL CONSTRAINT DF_terne_commands_requested_at DEFAULT SYSDATETIMEOFFSET();

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_commands_correlation' AND object_id = OBJECT_ID('terne_terminal_commands'))
  CREATE INDEX ix_terne_commands_correlation ON terne_terminal_commands(correlation_id) WHERE correlation_id IS NOT NULL;

-- Targeted: queued/active
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_commands_queued' AND object_id = OBJECT_ID('terne_terminal_commands'))
  CREATE INDEX ix_terne_commands_queued ON terne_terminal_commands(terminal_id, created_at ASC) WHERE status = 'QUEUED';

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_terne_commands_active' AND object_id = OBJECT_ID('terne_terminal_commands'))
  CREATE INDEX ix_terne_commands_active ON terne_terminal_commands(terminal_id, created_at ASC) WHERE status IN ('QUEUED','ACKED','RUNNING');

-- 3) TRIGGER updated_at (SQL Server)
-- terminals
IF OBJECT_ID('trg_terne_terminals_set_updated_at', 'TR') IS NULL
EXEC('
CREATE TRIGGER trg_terne_terminals_set_updated_at
ON terne_terminals
AFTER UPDATE
AS
BEGIN
  SET NOCOUNT ON;
  UPDATE t
    SET updated_at = SYSDATETIMEOFFSET()
  FROM terne_terminals t
  INNER JOIN inserted i ON i.id = t.id;
END
');

-- commands
IF OBJECT_ID('trg_terne_commands_set_updated_at', 'TR') IS NULL
EXEC('
CREATE TRIGGER trg_terne_commands_set_updated_at
ON terne_terminal_commands
AFTER UPDATE
AS
BEGIN
  SET NOCOUNT ON;
  UPDATE c
    SET updated_at = SYSDATETIMEOFFSET()
  FROM terne_terminal_commands c
  INNER JOIN inserted i ON i.id = c.id;
END
');
