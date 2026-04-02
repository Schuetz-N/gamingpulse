# GamingPulse — Architecture

## Pipeline Flow
```
Schedule Trigger (every 15 min)
    │
    ▼
RSS Feeds (7 sources, parallel)
    │
    ▼
Merge
    │
    ▼
Parse & Normalize (Code Node)
  - URL extraction
  - Source + category mapping
  - Text cleanup (strip HTML, limit to 1500 chars)
  - Groq prompt assembly
  - Max 3 articles per source per run
    │
    ▼
Loop over items
    │
    ▼
Dedup Check ──► POST /api/dedup/check ──► SQLite
    │
    ├── Already seen → Wait → next item
    │
    └── New article
            │
        LLM Summarize ──► POST /api/llm/summarize ──► Groq API
            │
        If: summary not empty
            │
            ├── yes → Build Telegram message
            │               │
            │           Send Telegram
            │               │
            │           Save Post ──► POST /api/posts ──► SQLite
            │               │
            │           Wait → next item
            │
            └── no  → Wait → next item
```

## Container Network

All containers share the `gamingpulse_net` Podman bridge network and communicate via DNS name.

| Container | DNS Name | Internal Port | Host Binding |
|---|---|---|---|
| gamingpulse-n8n | `n8n` | 5678 | 127.0.0.1:5678 |
| gamingpulse-backend | `backend` | 8080 | none (expose only) |
| gamingpulse-dashboard | `dashboard` | 80 | 127.0.0.1:3000 |

The backend has no host binding — it is only reachable from within the container network. The dashboard nginx container proxies `/api/` requests to `http://backend:8080/api/`.

## Security
```
Browser
  │  Bearer <API_AUTH_TOKEN>
  ▼
nginx (dashboard container)
  │  proxy_pass + proxy_pass_header Authorization
  ▼
Spring Boot
  │
  ApiKeyAuthFilter (OncePerRequestFilter)
  │  reads Authorization header
  │  compares against ${API_AUTH_TOKEN}
  │  sets SecurityContext if valid
  ▼
SecurityConfig
  │  /api/status/health → permitAll
  │  /actuator/health   → permitAll
  │  everything else    → authenticated
  ▼
Controller
```

n8n authenticates against the backend using the same `API_AUTH_TOKEN` via the "GamingPulse Backend" HTTP Header Auth credential.

## Database Schema

### posts

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| title | VARCHAR | Article title |
| link | VARCHAR UNIQUE | Article URL |
| source | VARCHAR | Feed source name |
| category | VARCHAR | gaming / hardware / proton |
| summary | VARCHAR(2000) | Groq-generated summary |
| posted_at | TIMESTAMP | Instant |
| success | BOOLEAN | Whether post succeeded |

### errors

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| source | VARCHAR | Which component errored |
| message | VARCHAR(2000) | Error description |
| url | VARCHAR | Related URL (nullable) |
| occurred_at | TIMESTAMP | Instant |

### service_health_records

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| service | VARCHAR(50) | backend / n8n / groq |
| status | VARCHAR(20) | up / down / degraded / starting |
| error_detail | VARCHAR(255) | Exception message (nullable) |
| checked_at | TIMESTAMP | Instant |

Indexed on `(service, checkedAt)` and `checkedAt` for history queries.

### seen_urls (dedup store)

Managed by `DeduplicationService` — schema depends on existing implementation.

## Health Checks

`HealthCheckService` polls external services every 30 seconds and persists results to `service_health_records`:

| Service | Endpoint | Auth |
|---|---|---|
| backend | self-reported | — |
| n8n | `http://n8n:5678/healthz` | none |
| groq | `https://api.groq.com/openai/v1/models` | Bearer |

Records older than 30 days are deleted nightly at 03:00 by a `@Scheduled` cleanup job. Same retention applies to `posts` and `errors`.

## Design Decisions

**Groq over local LLM** — Switched from Ollama (local GPU) to Groq API. Eliminates GPU dependency, simplifies the container stack, and keeps inference fast without hardware requirements.

**SQLite over PostgreSQL** — Single file, no server process, zero ops overhead. Spring Data JPA makes migration straightforward if needed.

**n8n over custom code** — Visual pipeline is easy to inspect and modify. Code nodes handle custom logic where needed. Workflow JSON is version-controlled.

**Podman over Docker** — Rootless by default, daemonless, OCI-compatible. No daemon running as root on the host.

**Bearer token over JWT** — Simple shared secret is sufficient for a single-client internal API. No token expiry to manage, no key rotation complexity.

**SSH tunnel over public exposure** — Dashboard and n8n are never publicly reachable. Access requires an active SSH session to the VPS.