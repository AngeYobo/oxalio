CREATE TABLE IF NOT EXISTS invoice_lines (
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   BIGINT      NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description  VARCHAR(512) NOT NULL,
    quantity     NUMERIC(19,2) NOT NULL,
    unit_price   NUMERIC(19,2) NOT NULL,
    vat_rate     NUMERIC(5,2)  NOT NULL,
    vat_amount   NUMERIC(19,2) NOT NULL,
    discount     NUMERIC(19,2) NOT NULL,
    line_total   NUMERIC(19,2) NOT NULL,
    product_code VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id ON invoice_lines(invoice_id);
