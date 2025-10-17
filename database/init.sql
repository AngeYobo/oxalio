CREATE USER oxalio WITH PASSWORD 'change_me';
CREATE DATABASE oxalio OWNER oxalio;
\c oxalio;
CREATE TABLE IF NOT EXISTS invoice(
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  status TEXT NOT NULL DEFAULT 'PENDING',
  payload_in JSONB
);
