# GamingPulse Architecture

## System Overview

GamingPulse runs as a 4-container Podman Compose stack on a local Linux desktop. All processing happens on-device — no cloud services, no API costs.

## Pipeline Flow

```
 TRIGGER (every 30 min)
     │
     ▼
 FETCH (8 RSS feeds in parallel)
     │
     ▼
 MERGE (combine all feed items)
     │
     ▼
 CLEAN (normalize text, extract best content field, assign source + category)
     │
     ▼
 LOOP (process one article at a time)
     │
     ▼
 DEDUP CHECK ──► Spring Boot API ──► SQLite
     │
     ├── Already seen → skip, next item
     │
     └── New article ↓
                      │
                 SUMMARIZE ──► Ollama (llama3.1:8b on AMD GPU)
                      │
                  FORMAT (hashtag + title + summary + link)
                      │
                  TELEGRAM (post to channel)
                      │
                  LOG POST ──► Spring Boot API ──► SQLite
                      │
                  WAIT (5 seconds, rate limit)
                      │
                  NEXT ITEM (loop back)
```

## Container Network

All containers share the `gamingpulse_default` Podman bridge network and communicate via DNS names:

| Container | DNS Name | Port | Accessible from host |
|---|---|---|---|
| gamingpulse-n8n | `n8n` | 5678 | http://localhost:5678 |
| gamingpulse-ollama | `ollama` | 11434 | http://localhost:11434 |
| gamingpulse-backend | `backend` | 8080 | http://localhost:8080 |
| gamingpulse-dashboard | `dashboard` | 3000 | http://localhost:3000 |

## Data Flow

### RSS → n8n
n8n polls 8 RSS feeds every 30 minutes. Each feed returns 10-50 items (titles, descriptions, links).

### n8n → Spring Boot (Dedup)
For each article, n8n sends the URL to `POST /api/dedup/check`. The backend normalizes the URL and checks SQLite. Returns `{"isNew": true/false}`.

### n8n → Ollama (Summarization)
New articles are sent to Ollama's `/api/generate` endpoint with a summarization prompt. The LLM returns a 2-3 sentence English summary. YouTube links skip this step.

### n8n → Telegram (Posting)
Formatted messages are sent via the Telegram Bot API. Posts include category hashtag, title, summary, and source link. Rate limited to one post per 5 seconds.

### n8n → Spring Boot (Logging)
After each successful Telegram post, n8n logs the article to `POST /api/posts` for dashboard display.

## Database Schema

### seen_urls
Tracks all URLs the system has encountered for deduplication.

| Column | Type | Purpose |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| url | VARCHAR(1000) UNIQUE | Normalized URL |
| source | VARCHAR | Which feed it came from |
| seen_at | TIMESTAMP | When first seen |

### posts
Records all articles posted to Telegram.

| Column | Type | Purpose |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| title | VARCHAR | Article title |
| link | VARCHAR UNIQUE | Article URL |
| source | VARCHAR | Feed source name |
| category | VARCHAR | gaming / hardware / proton |
| summary | VARCHAR(2000) | LLM-generated summary |
| posted_at | TIMESTAMP | When posted |
| success | BOOLEAN | Post succeeded? |

### errors
Logs pipeline errors for debugging.

| Column | Type | Purpose |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| source | VARCHAR | Which component errored |
| message | VARCHAR(2000) | Error description |
| url | VARCHAR | Related URL (if any) |
| occurred_at | TIMESTAMP | When it happened |

## GPU Configuration

Ollama runs inside a container with ROCm support. The container mounts `/dev/kfd` and `/dev/dri` for GPU access. No ROCm installation needed on the host.

| Setting | Value | Purpose |
|---|---|---|
| HSA_OVERRIDE_GFX_VERSION | 11.0.0 | Map gfx1100 to ROCm target |
| OLLAMA_FLASH_ATTENTION | 1 | Enable flash attention |
| HIP_VISIBLE_DEVICES | 0 | Use first GPU |
| OLLAMA_NUM_PARALLEL | 1 | One request at a time |
| OLLAMA_MAX_LOADED_MODELS | 1 | Keep one model in VRAM |

## Design Decisions

**Why Podman over Docker?** Pre-installed on Fedora/Nobara, rootless by default, daemonless, OCI-compatible. Zero license concerns.

**Why Ollama over cloud APIs?** Zero cost, full privacy, no rate limits, works offline. The RX 7900 GRE handles 8B models at 43 tok/s — more than enough for news summaries.

**Why n8n over custom code?** Visual workflow builder makes the pipeline transparent and easy to modify. Code nodes allow custom logic where needed. Best of both worlds.

**Why SQLite over PostgreSQL?** Single file, no server process, perfect for a local single-user application. Spring Data JPA makes it trivial to swap later if needed.

**Why file-based source configs?** Adding a new source should be as simple as dropping a YAML file. No database migration, no code change (beyond wiring in n8n).
