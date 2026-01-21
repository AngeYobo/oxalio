ALTER TABLE invoices ADD COLUMN fne_invoice_id VARCHAR(36);
ALTER TABLE invoices ADD COLUMN fne_reference VARCHAR(50);
ALTER TABLE invoice_lines ADD COLUMN fne_item_id VARCHAR(36);