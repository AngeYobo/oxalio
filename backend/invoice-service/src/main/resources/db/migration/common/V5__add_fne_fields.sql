ALTER TABLE invoices ADD COLUMN seller_display_name VARCHAR(255);
ALTER TABLE invoices ADD COLUMN point_of_sale_name VARCHAR(255);
ALTER TABLE invoices ADD COLUMN other_taxes DECIMAL(15,2);
ALTER TABLE invoices ADD COLUMN total_to_pay DECIMAL(15,2);
