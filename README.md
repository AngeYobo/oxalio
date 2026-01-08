# Oxalio Platform — FNE Côte d’Ivoire (Local/Demeter)

Plateforme modulaire conforme FNE (DGI CI). Cette distribution est optimisée pour **k3s/minikube** (Demeter).

## Modules
- backend
  - api-gateway (Spring Boot)
  - auth-service (Spring Boot OAuth2 client_credentials)
  - invoice-service (Spring Boot)
  - integration-service (Spring Boot, client DGI, mTLS + retry)
  - common (lib partagée)
- frontend (React + TypeScript + Vite) - login simulé + liste de factures
- infra (k8s manifests + terraform local)
- scripts (build / deploy / port-forward)
- docs (architecture + OpenAPI)
- database (init.sql)

## Démarrage rapide
```bash
# 1) Construire le backend (jar) et images docker locales
./scripts/build.sh

# 2) Déployer sur k8s local (namespace: oxalio)
./scripts/deploy.sh

# 3) Port-forward API gateway (si pas d'ingress) et lancer le frontend
./scripts/port-forward.sh &
cd frontend && npm install && npm run dev
# Frontend: http://localhost:3000 (proxy /api -> http://localhost:8080)
```
