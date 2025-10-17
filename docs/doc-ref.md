# 🧾 Oxalio FNE (Conformité DGI Côte d’Ivoire)

## 1. 🎯 Objectif du projet

Développer pour **Oxalio** une solution complète d’interfaçage avec le système **FNE (Facture Normalisée Électronique)** de la **Direction Générale des Impôts (DGI Côte d’Ivoire)**, afin d’obtenir l’**agrément officiel d’éditeur/intégrateur**.

### Buts concrets

* Permettre aux entreprises clientes d’émettre, valider, annuler et archiver leurs factures normalisées électroniques.
* Automatiser les échanges avec la DGI (sandbox → production).
* Garantir la conformité juridique, technique et sécuritaire.

---

## 2. 🧠 Architecture technique minimale (Oxalio FNE-ready)

### Composants

* **API Gateway (Spring Cloud Gateway)** : routage, sécurité, journalisation.
* **Auth Service** : gestion des certificats client X.509 et tokens DGI.
* **Invoice Service** : génération, validation, archivage, transmission FNE.
* **Integration Service** : connecteurs DGI, callbacks, audit.
* **Common module** : DTO partagés (InvoiceDTO, SellerDTO, etc.).
* **Database** : PostgreSQL (persistence) + H2 pour tests locaux.

### Flux de base

1. Client Oxalio → `/api/v1/invoices` → Gateway
2. Gateway → Invoice Service → génération JSON conforme FNE.
3. Invoice Service → DGI (mutual TLS + certificat X.509).
4. DGI → callback `/api/v1/webhooks/dgi-callback`.
5. Archivage + statut dans la base Oxalio.

---

## 3. 🧩 Endpoints REST à implémenter

| Endpoint                 | Méthode | Description                                 |
| ------------------------ | ------- | ------------------------------------------- |
| `/auth/token`            | POST    | Authentification mTLS, génération token DGI |
| `/invoices`              | POST    | Création d’une facture normalisée           |
| `/invoices/{id}`         | GET     | Consultation d’une facture                  |
| `/invoices/{id}/status`  | GET     | Vérification du statut auprès de la DGI     |
| `/invoices/{id}/cancel`  | POST    | Annulation d’une facture                    |
| `/webhooks/dgi-callback` | POST    | Réception des notifications DGI             |
| `/health` / `/readiness` | GET     | Monitoring de service                       |

**Obligatoire :**

* Versioning (`/api/v1/`)
* Headers : `Authorization`, `Idempotency-Key`, `Correlation-Id`
* Réponses JSON normalisées (`code`, `message`, `timestamp`, `data`)

---

## 4. 📦 Modèle JSON conforme FNE

```json
{
  "invoiceNumber": "OXA-2025-0001",
  "issueDate": "2025-10-06T09:00:00Z",
  "currency": "XOF",
  "invoiceType": "FACTURE_VENTE",
  "paymentMode": "ESPECES",
  "seller": {
    "nif": "CI123456789",
    "companyName": "Oxalio SARL",
    "address": "Abidjan Plateau"
  },
  "buyer": {
    "nif": "CI987654321",
    "name": "Client Test",
    "address": "Yopougon"
  },
  "lines": [
    {
      "designation": "Prestation de service",
      "quantity": 1,
      "unitPrice": 12500,
      "vatRate": 18,
      "vatAmount": 2250
    }
  ],
  "totals": {
    "subtotal": 12500,
    "totalVat": 2250,
    "totalAmount": 14750
  },
  "fneMetadata": {
    "qrCode": "base64-string",
    "hash": "SHA256-string",
    "signature": "base64-cert"
  }
}
```

---

## 5. 🔐 Authentification & Sécurité

**Mode unique validé par la DGI :**

* Authentification **mutuelle TLS (mTLS)** avec **certificat client X.509** fourni par la DGI.
* Vérification serveur via certificat racine DGI.
* Rotation et renouvellement automatisé des certificats.
* Toutes les connexions → HTTPS (TLS 1.2+ obligatoire).

**Protection applicative :**

* JWT interne signé par Oxalio pour clients B2B.
* Rate limiting et audit des accès via API Gateway.
* Logs structurés (`requestId`, `invoiceId`, `status`, `latency`).

---

## 6. ⚙️ Environnement & configuration

| Environnement  | URL                               | Mode                 |
| -------------- | --------------------------------- | -------------------- |
| Local          | `http://localhost:8082`           | Dev/test             |
| Sandbox DGI    | `https://fne-sandbox.dgi.gouv.ci` | Préproduction        |
| Production DGI | `https://fne.dgi.gouv.ci`         | Live (post-agrément) |

### Variables à prévoir

```
FNE_ENV=sandbox
FNE_CERT_PATH=/secrets/client-cert.pem
FNE_KEY_PATH=/secrets/client-key.pem
FNE_API_URL=https://fne-sandbox.dgi.gouv.ci
OXALIO_TOKEN_SECRET=********
DB_URL=jdbc:postgresql://localhost:5432/oxalio_fne
```

---

## 7. 🧪 Tests & validation DGI

### Tests internes

* Tests unitaires (mock DGI) : 100 % endpoints.
* Tests d’intégration réels : sandbox DGI.
* Cas de tests : création, validation, annulation, double envoi, erreurs réseau.

### Validation officielle

1. Génération et transmission d’un **jeu de factures normalisées**.
2. Présentation en **démonstration DGI Plateau**.
3. Validation de la conformité technique et sécurité.
4. Attribution d’un **numéro d’agrément Oxalio**.

---

## 8. 📊 Observabilité & audit

* **Logs** : corrélation `invoiceId` / `requestId`.
* **Audit trail** : journalisation immuable (base ou blockchain interne).
* **Metrics** : volume, temps de réponse, erreurs DGI.
* **Healthchecks** exposés via `/actuator/health`.

---

## 9. 📁 Livrables attendus

1. Code source (API Gateway, Auth, Invoice, Integration).
2. Spécification OpenAPI 3.1 documentée.
3. Rapport de tests (unitaires + sandbox DGI).
4. Fiche technique + captures FNE pour dossier d’agrément.
5. Documentation d’intégration (PDF ou Markdown).

---

## ✅ Critères de succès

| Domaine        | Objectif                               |
| -------------- | -------------------------------------- |
| Conformité DGI | 100 % des champs JSON validés          |
| Sécurité       | Connexion mTLS réussie                 |
| Disponibilité  | ≥ 99.9 % uptime                        |
| Performance    | Latence P95 < 500 ms                   |
| Agrément       | Validation sandbox + test sur site DGI |

