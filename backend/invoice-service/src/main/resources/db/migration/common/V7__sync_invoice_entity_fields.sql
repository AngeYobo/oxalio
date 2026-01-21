-- ============================================================
--  Flyway Migration V5 : Synchronisation de la table invoices
--  Compatible H2 / PostgreSQL / MySQL
--  Ajoute uniquement les colonnes manquantes (s'il y en a)
-- ============================================================

-- seller_display_name
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS seller_display_name VARCHAR(255);

-- point_of_sale_name
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS point_of_sale_name VARCHAR(255);

-- other_taxes
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS other_taxes DECIMAL(15,2);

-- total_to_pay
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS total_to_pay DECIMAL(15,2);
