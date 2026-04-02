# 🎮 GamingPulse

Self-hosted gaming news aggregator. Pulls RSS feeds from 7 sources, summarizes articles via Groq, and delivers curated posts to a Telegram channel. A Vue 3 dashboard provides live monitoring.

## Architecture
```
Schedule Trigger (every 15 min)
  → RSS Feeds (7 sources)
  → Merge
  → Loop over items
      → Dedup check        POST /api/dedup/check
      → LLM summarize      POST /api/llm/summarize  (Groq)
      → Send Telegram
      → Save post          POST /api/posts
```

## Tech Stack

| Layer | Technology |
|---|---|
| Workflow engine | n8n (Community Edition) |
| LLM | Groq API (llama-3.1-8b-instant) |
| Backend | Java 25 + Spring Boot 4 + Maven |
| Database | SQLite (via Hibernate) |
| Dashboard | Vue 3 + TypeScript + Vite + Chart.js |
| Proxy | nginx (inside dashboard container) |
| Containers | Podman + podman-compose (rootless) |
| CI/CD | GitHub Actions → SSH → Contabo VPS |
| Messaging | Telegram Bot API |

## Content Sources

| Source | Category |
|---|---|
| PC Gamer | #gaming |
| Rock Paper Shotgun | #gaming |
| Eurogamer | #gaming |
| TechPowerUp | #hardware |
| Phoronix | #hardware |
| Digital Foundry (YouTube) | #hardware |
| GamingOnLinux | #proton |

## Services

| Service | Port | Access |
|---|---|---|
| Dashboard | 3000 | SSH tunnel only |
| n8n | 5678 | SSH tunnel only |
| Backend | 8080 | Internal only (no public port) |

All ports are bound to `127.0.0.1` — no public exposure except SSH.

## Setup

### Prerequisites

- Contabo VPS (or any Linux server) with SSH access
- Podman + podman-compose
- GitHub account (for Actions CI/CD)
- Telegram Bot Token (via [@BotFather](https://t.me/BotFather))
- Groq API key ([console.groq.com](https://console.groq.com))

### Configure
```bash
git clone https://github.com/Schuetz-N/gamingpulse.git
cd gamingpulse
cp .env.example .env
nano .env
```

### Deploy

Push to `main` triggers the GitHub Actions pipeline automatically:
```
push → build backend JAR → build dashboard → copy to VPS → podman-compose up
```

### Access via SSH tunnel
```bash
# Dashboard
ssh -L 3000:localhost:3000 user@000.00.00.00 (Server-User@IP-Address)

# n8n
ssh -L 5678:localhost:5678 user@000.00.00.00 (Server-User@IP-Address)
```

> Always start the stack before opening the tunnel — not the other way around.

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/status` | Bearer | System status + stats |
| GET | `/api/status/health` | Public | Health check (used by Podman) |
| GET | `/api/status/history` | Bearer | Service uptime history |
| POST | `/api/dedup/check` | Bearer | Check + mark URL as seen |
| GET | `/api/dedup/count` | Bearer | Number of tracked URLs |
| GET | `/api/posts` | Bearer | Paginated post list |
| POST | `/api/posts` | Bearer | Save a post (called by n8n) |
| GET | `/api/posts/stats` | Bearer | Post count by source |
| GET | `/api/posts/history` | Bearer | Post activity over time |
| GET | `/api/errors` | Bearer | Last 100 errors |
| POST | `/api/errors` | Bearer | Log an error (called by n8n) |
| GET | `/api/errors/stats` | Bearer | Error count by source |

All authenticated endpoints require `Authorization: Bearer <API_AUTH_TOKEN>`.

## Project Structure
```
gamingpulse/
├── .github/workflows/
│   └── deploy.yml                  # CI/CD pipeline
├── podman-compose.yml
├── .env.example
├── scripts/
│   ├── start.sh
│   └── stop.sh
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/dev/gamingpulse/
│       │   ├── config/             # Security, CORS, Web
│       │   ├── controller/         # REST endpoints
│       │   ├── model/              # JPA entities
│       │   ├── repository/         # Data access
│       │   ├── security/           # API key filter
│       │   └── service/            # Business logic
│       └── resources/
│           └── application.properties
├── dashboard/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
│       ├── api/
│       │   └── client.ts           # API client
│       ├── components/
│       │   ├── charts/             # ActivityChart, UptimePulse
│       │   └── ui/                 # StatusBadge, StatCard, ...
│       ├── composables/            # useStatus, usePosts, useErrors, ...
│       ├── types/
│       │   └── index.ts            # Shared TypeScript interfaces
│       ├── views/                  # StatusView, PostsView, ErrorsView
│       ├── assets/
│       │   └── main.css
│       ├── App.vue
│       └── main.ts
└── n8n-workflows/
    └── GamingPulse_-_RSS_Pipeline.json
```

## Secrets

All secrets are stored as GitHub Actions Secrets and written to `/opt/gamingpulse/.env` on the VPS during deployment.

| Secret | Description |
|---|---|
| `VPS_HOST` | SSH target |
| `VPS_USER` | SSH user |
| `VPS_SSH_KEY` | SSH private key |
| `VPS_SSH_PASSPHRASE` | SSH key passphrase |
| `N8N_PASSWORD` | n8n basic auth password |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token |
| `TELEGRAM_CHANNEL_ID` | Target channel ID |
| `GROQ_API_KEY` | Groq API key |
| `API_AUTH_TOKEN` | Internal shared secret (openssl rand -hex 32) |

## Adding a Source

1. Add the domain to `sourceMap` and `getDomain` in the **Parse Feed Items** Code node in n8n:
```javascript
// sourceMap
'arstechnica.com': { source: 'Ars Technica', category: 'gaming' },

// getDomain
if (url.includes('arstechnica.com')) return 'arstechnica.com';
```

2. Add a new **RSS Feed Read** node in n8n with the feed URL
3. Connect **Schedule Trigger** → new node → **Merge**
4. Export the updated workflow JSON and commit to `n8n-workflows/`

### Finding RSS feed URLs

Most sites follow one of these patterns: `/rss`, `/feed`, `/rss.xml`. YouTube channels use `https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID`.
## License

MIT