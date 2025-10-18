# Spécifications API FNE — Draft Oxalio

## 0) Principes

* **Rôle** : réception de factures normalisées, contrôle, **signature/sticker**, génération **QR** et numéro FNE, puis restitution et archivage.
* **Formats** : `application/json` (transport) ; optionnels `application/pdf`/`image/png` pour rendus.
* **Horodatage** : ISO-8601 UTC (`2025-10-18T12:34:56.789Z`).
* **Idempotence** : header `Idempotency-Key` requis sur les POST/PUT (UUID v4 recommandé).
* **Versionnage** : préfixe d’URL (`/v1`).
* **Environnements**

  * `mock`: local (boucle immédiate, réponses simulées)
  * `sandbox`: DGI bac à sable (auth et règles souples)
  * `prod`: DGI production (contrôles stricts)

Base URL (exemples)

* Mock: `http://localhost:3005`
* Sandbox: `https://sandbox.dgi.gov.ci/fne/api/v1`
* Prod: `https://fne.dgi.gov.ci/api/v1`

---

## 1) Sécurité

* **Auth** : `Authorization: Bearer <access_token>`
* **Signature requêtes** (optionnel mais recommandé) :
  `X-Signature: <base64(hmac-sha256(body, client_secret))>`
* **Horodatage anti-replay** : `X-Timestamp: 2025-10-18T12:34:56Z` (+/- 5 min)
* **TLS** : obligatoire (HTTPS)

---

## 2) Statuts métiers

`RECEIVED` → `VALIDATING` → `ACCEPTED` → `SIGNED`
`REJECTED` (à n’importe quelle étape)
`CANCELLED` (annulation légale)
`CREDITED` (via avoir)

---

## 3) Ressources & Endpoints

### 3.1 Factures

**Créer / soumettre une facture**
`POST /v1/invoices`
Headers: `Authorization`, `Idempotency-Key`, `Content-Type: application/json`

Request (exemple minimal viable) :

```json
{
  "invoiceNumber": "INV-2025-0001",
  "issueDate": "2025-10-18T11:10:00Z",
  "currency": "XOF",
  "invoiceType": "STANDARD",
  "paymentMode": "CASH",
  "seller": { "taxId": "NIF123456A", "companyName": "Oxalio SARL", "address": "Abidjan, Côte d’Ivoire" },
  "buyer":  { "taxId": "NIF789012B", "name": "Client Démo", "address": "Bouaké, Côte d’Ivoire" },
  "lines": [
    { "description": "Prestation", "quantity": 1, "unitPrice": 10000, "vatRate": 18, "discount": 0 }
  ],
  "totals": { "subtotal": 10000, "totalVat": 1800, "totalAmount": 11800 },
  "metadata": { "channel": "ERP", "externalId": "OX-abc-123" },
  "notify": { "webhookUrl": "https://oxalio.app/hooks/fne" }
}
```

Response `202 Accepted` (synchrone minimal) :

```json
{
  "reference": "FNE-24-0000009876",
  "status": "RECEIVED",
  "message": "Document queued for validation",
  "links": {
    "self": "/v1/invoices/FNE-24-0000009876",
    "events": "/v1/invoices/FNE-24-0000009876/events"
  }
}
```

> **Remarque** : suivant l’environnement, la FNE peut déjà retourner `SIGNED` avec `qrCode` et `signature` si la validation est instantanée.

---

**Consulter une facture**
`GET /v1/invoices/{reference}`

Response (signée) :

```json
{
  "reference": "FNE-24-0000009876",
  "status": "SIGNED",
  "invoiceNumber": "INV-2025-0001",
  "fiscalYear": 2025,
  "issueDate": "2025-10-18T11:10:00Z",
  "currency": "XOF",
  "invoiceType": "STANDARD",
  "paymentMode": "CASH",
  "seller": { "taxId": "NIF123456A", "companyName": "Oxalio SARL", "address": "Abidjan, Côte d’Ivoire" },
  "buyer":  { "taxId": "NIF789012B", "name": "Client Démo", "address": "Bouaké, Côte d’Ivoire" },
  "lines": [
    { "description": "Prestation", "quantity": 1, "unitPrice": 10000, "vatRate": 18, "vatAmount": 1800, "discount": 0 }
  ],
  "totals": { "subtotal": 10000, "totalVat": 1800, "totalAmount": 11800 },
  "signature": "base64-signature-or-jws",
  "qrCode": "data:image/png;base64,iVBORw0KGgo...",
  "hash": "SHA256:abcf...",
  "processedAt": "2025-10-18T11:10:05.222Z",
  "events": [
    { "at": "2025-10-18T11:10:01Z", "status": "VALIDATING" },
    { "at": "2025-10-18T11:10:05Z", "status": "SIGNED" }
  ]
}
```

---

**Lister les factures**
`GET /v1/invoices?status=SIGNED&from=2025-10-01&to=2025-10-31&page=1&size=50&sort=issueDate,desc`

Response:

```json
{
  "page": 1,
  "size": 50,
  "totalElements": 1234,
  "content": [
    { "reference": "FNE-24-....", "invoiceNumber": "INV-...", "status": "SIGNED", "totalAmount": 11800, "currency": "XOF", "issueDate": "2025-10-18T11:10:00Z" }
  ]
}
```

---

**Récupérer un PDF « officiel »**
`GET /v1/invoices/{reference}/pdf` → `application/pdf`

**Récupérer le QR Code seul**
`GET /v1/invoices/{reference}/qrcode` → `image/png`

---

**Annuler une facture (storno)**
`POST /v1/invoices/{reference}/cancel`
Body:

```json
{ "reasonCode": "CUSTOMER_RETURN", "reason": "Commande annulée par le client" }
```

Response:

```json
{ "reference": "FNE-24-0000009876", "status": "CANCELLED", "cancelledAt": "2025-10-18T12:00:11Z" }
```

---

**Avoir**
`POST /v1/invoices/{reference}/credit-note`
Request (mêmes champs qu’une facture ; référence l’originale)
Response: `201 Created` avec nouvelle référence `CREDITED`.

---

**Évènements / audit**
`GET /v1/invoices/{reference}/events` → historique statuts & contrôles

---

### 3.2 Administration / Quotas

**Solde de stickers/licences**
`GET /v1/stickers/balance` → `{ "remaining": 12345 }`

**Santé**
`GET /v1/health` → `{ "status": "UP", "time": "..." }`

---

## 4) Modèle de données (extraits)

```json
// InvoiceLine
{
  "description": "string (1..512)",
  "quantity": 1.0,
  "unitPrice": 10000.0,
  "vatRate": 18.0,
  "vatAmount": 1800.0,
  "discount": 0.0
}

// Party
{
  "taxId": "NIF123456A",       // Optionnel pour particuliers
  "companyName": "Oxalio SARL",
  "name": "Client Démo",      // Si particulier
  "address": "Abidjan, Côte d’Ivoire"
}

// Totals
{
  "subtotal": 10000.0,
  "totalVat": 1800.0,
  "totalAmount": 11800.0
}
```

**Contraintes clés**

* `invoiceNumber` : **unique** par émetteur et **séquentiel** par année fiscale.
* `currency` : ISO-4217 (`XOF`, `EUR`, …).
* Sommes cohérentes : `totalAmount = subtotal - totalDiscount + totalVat`.
* TVA : `vatAmount` par ligne **ou** calculable via `quantity * unitPrice * vatRate/100`.

---

## 5) Erreurs & codes

| HTTP | code                   | message                        | action Oxalio               |
| ---- | ---------------------- | ------------------------------ | --------------------------- |
| 400  | `INVALID_SCHEMA`       | Champ manquant/format invalide | Corriger et réémettre       |
| 401  | `UNAUTHORIZED`         | Token absent/expiré            | Rafraîchir token            |
| 403  | `FORBIDDEN`            | Droits insuffisants            | Vérifier habilitation       |
| 404  | `NOT_FOUND`            | Référence inconnue             | Vérifier référence          |
| 409  | `DUPLICATE_NUMBER`     | Numéro déjà utilisé            | Générer nouveau numéro      |
| 422  | `BUSINESS_RULE_FAILED` | Règle fiscale violée           | Corriger données            |
| 429  | `RATE_LIMITED`         | Trop de requêtes               | Backoff + retry             |
| 500  | `INTERNAL_ERROR`       | Erreur FNE                     | Retry exponentiel + support |
| 503  | `SERVICE_UNAVAILABLE`  | Maintenance FNE                | Retry après `Retry-After`   |

**Structure d’erreur**

```json
{
  "timestamp": "2025-10-18T11:12:00Z",
  "httpStatus": 422,
  "code": "BUSINESS_RULE_FAILED",
  "message": "VAT total mismatch with line items",
  "details": [{ "field": "totals.totalVat", "issue": "expected=1800, got=1700" }],
  "correlationId": "2d6f0d4a-3b5e-4ee7-86b0-2e7a1b7f7a23"
}
```

---

## 6) Webhooks FNE → Oxalio (optionnels mais recommandés)

`POST {notify.webhookUrl}` (ex: `https://oxalio.app/hooks/fne`)
Headers: `X-FNE-Signature: base64(hmac-sha256(body, webhook_secret))`

Payload (exemple) :

```json
{
  "reference": "FNE-24-0000009876",
  "status": "SIGNED",
  "invoiceNumber": "INV-2025-0001",
  "processedAt": "2025-10-18T11:10:05.222Z",
  "signature": "base64-signature-or-jws",
  "qrCode": "data:image/png;base64,iVBORw0K..."
}
```

> **Sécurité** : vérifier la signature ; répondre `2xx`. En cas d’échec, la FNE retente (`max=6`, backoff exponentiel).

---

## 7) Idempotence & retries

* `Idempotency-Key` (UUID) **obligatoire** sur `POST /invoices`, `cancel`, `credit-note`.
* Serveur FNE doit retourner **toujours la même réponse** pour la même clé pendant 24h.
* **Retries côté client** :

  * 5xx/`429` : retry exponentiel (`1s, 2s, 4s, 8s, 16s`, jitter).
  * Jamais re-poster **sans** la même `Idempotency-Key`.

---

## 8) Limites & pagination

* `Rate-Limit-Limit`, `Rate-Limit-Remaining`, `Rate-Limit-Reset` dans la réponse.
* Pagination : `page` (1-based), `size` (≤ 200), `sort=field,asc|desc`.

---

## 9) Squelette OpenAPI 3.1 (extrait)

```yaml
openapi: 3.1.0
info:
  title: FNE API (Draft Oxalio)
  version: 1.0.0
servers:
  - url: https://fne.dgi.gov.ci/api/v1
  - url: https://sandbox.dgi.gov.ci/fne/api/v1
  - url: http://localhost:3005
paths:
  /invoices:
    post:
      summary: Soumettre une facture pour signature FNE
      operationId: submitInvoice
      parameters:
        - in: header
          name: Idempotency-Key
          required: true
          schema: { type: string }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/InvoiceSubmit' }
      responses:
        '202': { $ref: '#/components/responses/AcceptedRef' }
        '409': { $ref: '#/components/responses/Error' }
        '422': { $ref: '#/components/responses/Error' }
  /invoices/{reference}:
    get:
      summary: Consulter une facture
      parameters:
        - in: path
          name: reference
          required: true
          schema: { type: string }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { $ref: '#/components/schemas/InvoiceSigned' }
        '404': { $ref: '#/components/responses/Error' }
components:
  schemas:
    Party:
      type: object
      properties:
        taxId: { type: string }
        companyName: { type: string }
        name: { type: string }
        address: { type: string }
    Line:
      type: object
      required: [description, quantity, unitPrice]
      properties:
        description: { type: string, minLength: 1, maxLength: 512 }
        quantity: { type: number, minimum: 0 }
        unitPrice: { type: number, minimum: 0 }
        vatRate: { type: number, minimum: 0, maximum: 100 }
        vatAmount: { type: number, minimum: 0 }
        discount: { type: number, minimum: 0 }
    Totals:
      type: object
      required: [subtotal, totalVat, totalAmount]
      properties:
        subtotal: { type: number }
        totalVat: { type: number }
        totalAmount: { type: number }
    InvoiceSubmit:
      type: object
      required: [invoiceNumber, issueDate, currency, seller, buyer, lines, totals]
      properties:
        invoiceNumber: { type: string }
        issueDate: { type: string, format: date-time }
        currency: { type: string, minLength: 3, maxLength: 3 }
        invoiceType: { type: string, enum: [STANDARD, CREDIT_NOTE, PROFORMA] }
        paymentMode: { type: string, enum: [CASH, CARD, TRANSFER, MOBILE] }
        seller: { $ref: '#/components/schemas/Party' }
        buyer: { $ref: '#/components/schemas/Party' }
        lines:
          type: array
          items: { $ref: '#/components/schemas/Line' }
          minItems: 1
        totals: { $ref: '#/components/schemas/Totals' }
        metadata: { type: object, additionalProperties: true }
        notify:
          type: object
          properties:
            webhookUrl: { type: string, format: uri }
    InvoiceSigned:
      allOf:
        - $ref: '#/components/schemas/InvoiceSubmit'
        - type: object
          properties:
            reference: { type: string }
            status: { type: string, enum: [RECEIVED, VALIDATING, ACCEPTED, SIGNED, REJECTED, CANCELLED, CREDITED] }
            signature: { type: string }
            qrCode: { type: string }
            hash: { type: string }
            processedAt: { type: string, format: date-time }
  responses:
    AcceptedRef:
      description: Accepted
      content:
        application/json:
          schema:
            type: object
            properties:
              reference: { type: string }
              status: { type: string }
              message: { type: string }
              links: { type: object, additionalProperties: { type: string } }
    Error:
      description: Error
      content:
        application/json:
          schema:
            type: object
            properties:
              timestamp: { type: string, format: date-time }
              httpStatus: { type: integer }
              code: { type: string }
              message: { type: string }
              details:
                type: array
                items:
                  type: object
                  properties:
                    field: { type: string }
                    issue: { type: string }
              correlationId: { type: string }
```

---

## 10) Checklist côté Oxalio

* [ ] Ajouter `Idempotency-Key` à `DgiClient` + gestion retry/jitter.
* [ ] Support des **3 environnements** via profils Spring (`mock`, `sandbox`, `prod`).
* [ ] Mapper `InvoiceDTO → InvoiceSubmit` (déjà amorcé).
* [ ] Persister `reference`, `status`, `signature`, `qrCode`, `hash`, `processedAt`.
* [ ] Exposer un **webhook** `/callbacks/fne` (vérifier `X-FNE-Signature`).
* [ ] Générer **PDF** local avec sticker et QR (fallback si FNE ne renvoie pas de PDF).
* [ ] Traçabilité : journal d’évènements + table d’audit immuable.

---

