\echo '--- Smoke test terminals updated_at ---'

INSERT INTO terne_terminals (
  tenant_id, status, serial_number, manufacturer, model, os_version, created_at, updated_at
)
VALUES (
  gen_random_uuid(), 'ENROLLED', 'SN-SMOKE-001', 'PAX', 'A920', 'Android-13', now(), now()
)
ON CONFLICT (serial_number) DO NOTHING;

UPDATE terne_terminals
SET app_version = '1.0.1'
WHERE serial_number = 'SN-SMOKE-001';

SELECT serial_number, created_at, updated_at
FROM terne_terminals
WHERE serial_number = 'SN-SMOKE-001';

\echo '--- Smoke test queued commands ---'

INSERT INTO terne_terminal_commands (
  terminal_id, command_type, status, payload, requested_by
)
SELECT
  id, 'SYNC_CONFIG', 'QUEUED', '{"scope":"all"}'::jsonb, 'smoke-test'
FROM terne_terminals
WHERE serial_number = 'SN-SMOKE-001';

EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM terne_terminal_commands
WHERE status = 'QUEUED'
ORDER BY created_at ASC
LIMIT 10;
