-- Ajout/alignement des colonnes attendues par InvoiceLineEntity
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS sku           VARCHAR(64);
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS unit          VARCHAR(16);

-- description (déjà présente normalement)
-- ALTER TABLE invoice_lines ALTER COLUMN description SET NOT NULL; -- décommente si tu veux forcer

-- nombres (avec defaults pour éviter les NOT NULL errors)
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS quantity      DECIMAL(19,3) DEFAULT 0 NOT NULL;
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS unit_price    DECIMAL(19,2) DEFAULT 0 NOT NULL;
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS vat_rate      DECIMAL(5,2)  DEFAULT 0 NOT NULL;
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS vat_amount    DECIMAL(19,2) DEFAULT 0 NOT NULL;
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS discount      DECIMAL(19,2) DEFAULT 0 NOT NULL;
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS line_total    DECIMAL(19,2) DEFAULT 0 NOT NULL;

-- compat héritée
ALTER TABLE invoice_lines ADD COLUMN IF NOT EXISTS product_code  VARCHAR(100);

-- (l’index invoice_id existe déjà via V2; sinon:)
-- CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id ON invoice_lines(invoice_id);
