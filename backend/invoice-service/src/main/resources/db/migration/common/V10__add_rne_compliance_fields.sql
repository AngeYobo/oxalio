-- 1. Ajout des nouveaux champs RNE
ALTER TABLE invoices ADD COLUMN is_rne BOOLEAN DEFAULT FALSE;
ALTER TABLE invoices ADD COLUMN template VARCHAR(10);
ALTER TABLE invoices ADD COLUMN rne VARCHAR(64);

-- 2. Gestion du mode de paiement (Harmonisation avec l'entité)
-- On vérifie si payment_mode existe pour le renommer, 
-- sinon on crée payment_method directement.
ALTER TABLE invoices ALTER COLUMN payment_mode RENAME TO payment_method;