# Rapport d’architecture — Oxalio (Local/Demeter)

## Objectifs
- Conformité FNE (création, statut, annulation) avec journalisation immuable.
- Sécurité: TLS 1.3, mTLS (webhook DGI plus tard), AES-256 at-rest, OAuth2 (client_credentials), RBAC.
- Disponibilité >99.9% (cible prod), P95 < 500ms (cible prod), asynchrone avec Kafka.

## Architecture logique
- API Gateway: termin. TLS, auth JWT, rate-limit, routing.
- Auth Service: émission de tokens (client_credentials), JWK set.
- Invoice Service: validation JSON, calcul totaux, statut/annulation, audit.
- Integration Service: client DGI (mTLS, retry/backoff, circuit-breaker), webhook `/webhooks/dgi-callback`.
- PostgreSQL (persistance), Redis (cache), Kafka (asynchrone).
- Vault (secrets) — simulé ici par Secrets K8s.
- Monitoring: /actuator + Prometheus (à brancher), logs corrélés (X-Correlation-Id).

## Environnements
- dev (local Demeter), test-dgi (sandbox), preprod, prod.
- Ségrégation stricte, IaC (terraform) et k8s manifests versionnés.

## Flux principal
1. `POST /api/invoices` (Idempotency-Key requis) → validation locale + persist(PENDING).
2. Appel DGI (Integration Service) → update statut: VALIDATED/REJECTED (sync/async).
3. Archiver + journal d’audit append-only.
