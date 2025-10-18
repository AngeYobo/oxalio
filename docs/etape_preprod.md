## 🧭 Plan de travail « Pré-production »

| Étape                         | Objectif                                                                               | Statut attendu |
| ----------------------------- | -------------------------------------------------------------------------------------- | -------------- |
| 1. Domain Model & Statuts DGI | Enrichir le modèle de données pour gérer les états (DRAFT → SENT → VALIDATED/REJECTED) | OK             |
| 2. DGI Client                 | Créer un service capable d’appeler un endpoint externe et parser la réponse            | OK             |
| 3. Service Layer              | Intégrer la logique d’envoi + mise à jour du statut                                    | OK             |
| 4. Logs & Audit Trail         | Tracer les événements (envoi, réponse, erreurs)                                        | OK             |
| 5. Configuration              | Préparer `application.yml` pour endpoint réel + bascule Postgres                       | OK             |
| 6. Tests & Gateway            | Vérifier les flux via `/api/v1/invoices/send/{id}` en passant par la gateway           | OK             |

---

## 1️⃣ — Domain Model & Statuts DGI

Dans `InvoiceEntity.java` (ou DTO si persistence à part), ajoute ces champs :

```java
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String currency;
    private String status; // DRAFT, SENT, VALIDATED, REJECTED

    @Lob
    private String dgiResponse; // JSON brut de la DGI

    private Instant createdAt;
    private Instant updatedAt;

    // ... autres champs (seller, buyer, totals, etc.)

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // getters/setters
}
```

👉 Objectif : pouvoir stocker aussi bien la **facture** que le **résultat de validation DGI**.

---

## 2️⃣ — DGI Client (service d’appel externe)

Crée un fichier :
`src/main/java/com/oxalio/invoice/client/DgiClient.java`

```java
package com.oxalio.invoice.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class DgiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DGI_ENDPOINT = "https://sandbox.dgi.gov.ci/api/fne/invoices"; // à adapter

    public String sendInvoice(Object invoicePayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // ajouter token d’authentification si nécessaire
        HttpEntity<Object> entity = new HttpEntity<>(invoicePayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(DGI_ENDPOINT, entity, String.class);
        return response.getBody();
    }
}
```

📝 On commence par une version simple.
Ensuite on ajoutera :

* auth header (token DGI),
* gestion d’erreurs,
* timeouts,
* retry.

---

## 3️⃣ — Service Layer (intégration envoi)

Dans `InvoiceService.java` :

```java
public Invoice sendInvoiceToDGI(Long id) {
    Invoice invoice = invoiceRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Invoice not found"));

    // mapper vers format DGI
    Object payload = dgiMapper.toDgiPayload(invoice);

    try {
        String response = dgiClient.sendInvoice(payload);
        invoice.setDgiResponse(response);
        invoice.setStatus("VALIDATED"); // ou "REJECTED" selon la réponse
    } catch (Exception ex) {
        invoice.setDgiResponse("{\"error\":\"" + ex.getMessage() + "\"}");
        invoice.setStatus("REJECTED");
    }

    invoiceRepository.save(invoice);
    return invoice;
}
```

---

## 4️⃣ — Logs & Audit Trail

Ajoute un logger dans le service :

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
```

Et logue chaque étape :

```java
log.info("Sending invoice {} to DGI", invoice.getInvoiceNumber());
log.info("DGI response: {}", response);
log.error("Error sending invoice: {}", ex.getMessage());
```

---

## 5️⃣ — Configuration pour la bascule

Dans `application.yml` (ajouter à la fin) :

```yaml
dgi:
  endpoint: https://sandbox.dgi.gov.ci/api/fne/invoices
  auth-token: CHANGER_CECI
  timeout-ms: 10000
```

👉 Ensuite on injectera ces valeurs via `@Value` ou `@ConfigurationProperties` dans `DgiClient`.

---

## 6️⃣ — Endpoint REST

Dans `InvoiceController.java` :

```java
@PostMapping("/invoices/send/{id}")
public ResponseEntity<Invoice> sendInvoice(@PathVariable Long id) {
    Invoice updated = invoiceService.sendInvoiceToDGI(id);
    return ResponseEntity.ok(updated);
}
```

Test :

```bash
curl -X POST http://localhost:8082/invoices/send/1
```

Résultat attendu :

```json
{
  "id": 1,
  "invoiceNumber": "INV-2025-0001",
  "status": "VALIDATED",
  "dgiResponse": "{...}"
}
```

---

## ✅ Prochaine action concrète

1. [ ] Ajouter `status`, `dgiResponse` et timestamps dans `InvoiceEntity`.
2. [ ] Créer `DgiClient.java` avec `RestTemplate`.
3. [ ] Ajouter `sendInvoiceToDGI` dans `InvoiceService`.
4. [ ] Exposer `/invoices/send/{id}` dans le contrôleur.
5. [ ] Loguer tout le process.
6. [ ] Tester avec un payload mocké.

---

