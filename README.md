# 🎮 GamingPulse

Automated gaming news pipeline that collects content from 8 sources, summarizes articles using a local LLM, and delivers curated posts to a Telegram channel — entirely self-hosted, zero cost.

## What it does

GamingPulse polls RSS feeds from major gaming and tech outlets every 30 minutes. New articles are deduplicated against a SQLite database, summarized by a locally-running LLM (Ollama), formatted with category hashtags, and posted to a private Telegram channel with link previews. A Vue 3 dashboard provides real-time monitoring of all services.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Podman Compose Stack                      │
│                                                              │
│  ┌───────────┐    ┌───────────┐    ┌──────────────────────┐  │
│  │   n8n     │───▶│  Ollama  │    │   Spring Boot API    │  │
│  │  :5678    │◀───│  :11434  │    │       :8080          │  │
│  │           │    │           │    │                      │  │
│  │ Workflows │    │ ROCm GPU  │    │ - Dedup (SQLite)     │  │
│  │ RSS Feeds │    │ llama3.1  │    │ - Post history       │  │
│  │ Telegram  │    │ 8B Q8     │    │ - Health checks      │  │
│  └─────┬─────┘    └───────────┘    │ - Dashboard REST API │  │
│        │               ▲           └──────────┬───────────┘  │
│        │               │                      │              │
│        │          ┌────┴───────┐    ┌─────────┴──────────┐   │
│        │          │ AMD GPU    │    │   Vue 3 Dashboard  │   │
│        │          │ RX7900 GRE │    │      :3000         │   │
│        │          │ 16GB VRAM  │    │                    │   │
│        │          └────────────┘    │ - Service status   │   │
│        ▼                           │ - Post history      │   │
│  ┌───────────┐                     │ - Error log         │   │
│  │  SQLite   │◀────────────────────┘                    │   │
│  └───────────┘                                           │   │
└──────────────────────────────────────────────────────────────┘
        │
        ▼
  RSS / YouTube / GitHub ──▶ Telegram Channel
```

## Tech Stack

| Layer | Technology |
|---|---|
| Workflow Engine | n8n (Community Edition) |
| LLM Inference | Ollama + llama3.1:8b via ROCm |
| GPU | AMD Radeon RX 7900 GRE (16GB VRAM) |
| Backend | Java 25 + Spring Boot 4 + Maven |
| Database | SQLite (dedup, post history, error log) |
| Dashboard | Vue 3 + TypeScript + Vite |
| Containers | Podman + podman-compose (rootless) |
| Messaging | Telegram Bot API |
| OS | Nobara Linux (Fedora-based) |

## Content Sources

### #gaming
- **PC Gamer** — pcgamer.com/rss
- **Rock Paper Shotgun** — rockpapershotgun.com/feed/news
- **Eurogamer** — eurogamer.net/feed

### #hardware
- **TechPowerUp** — techpowerup.com/rss/news
- **Phoronix** — phoronix.com/rss.php
- **Digital Foundry** — YouTube RSS feed

### #proton
- **GamingOnLinux** — gamingonlinux.com/article_rss.php
- **OptiScaler** — GitHub Releases Atom feed

## Features

- **Automatic summarization** — Articles are summarized in 2-3 sentences by a local LLM
- **YouTube video previews** — YouTube links are posted without Markdown so Telegram renders the video preview
- **Smart deduplication** — URL normalization + SQLite-backed dedup prevents duplicate posts across runs
- **Rate limit handling** — Posts are queued with 5-second intervals to stay under Telegram's API limits
- **Category tagging** — Each post gets a hashtag (#gaming, #hardware, #proton) based on the source
- **Link previews** — All posts include the source URL for Telegram to generate previews
- **Plugin-based sources** — Add new sources by dropping a YAML config file
- **Self-hosted & free** — Zero external API costs, all inference runs on local GPU
- **Health monitoring** — Dashboard shows real-time status of all 4 services

## Services

| Service | Port | Purpose |
|---|---|---|
| n8n | :5678 | Workflow automation, RSS polling, Telegram posting |
| Ollama | :11434 | Local LLM inference with AMD GPU acceleration |
| Spring Boot | :8080 | REST API for dedup, post history, health checks |
| Dashboard | :3000 | Vue 3 monitoring UI |

## Quick Start

### Prerequisites

- Podman + podman-compose
- AMD GPU with ROCm support (or modify for CPU-only)
- Telegram Bot Token ([create via @BotFather](https://t.me/BotFather))

### Setup

```bash
git clone https://github.com/YOUR_USERNAME/gamingpulse.git
cd gamingpulse

# Configure
cp .env.example .env
nano .env  # Add your Telegram bot token and channel ID

# Start
./scripts/start.sh

# Access
# Dashboard:  http://localhost:3000
# n8n:        http://localhost:5678
# Backend:    http://localhost:8080/api/status
```

### First Run

On the first start, the pipeline will process all current articles from all feeds. After the initial run, only new articles trigger posts (typically 1-5 per 30-minute cycle).

## Project Structure

```
gamingpulse/
├── podman-compose.yml          # All 4 services
├── .env.example                # Environment template
├── scripts/
│   ├── start.sh                # Start all services
│   └── stop.sh                 # Stop all services
├── sources/                    # Source configs (YAML)
│   ├── pcgamer.yml
│   ├── eurogamer.yml
│   └── ...
├── backend/                    # Java 25 + Spring Boot 4
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/dev/gamingpulse/
│       ├── controller/         # REST endpoints
│       ├── model/              # JPA entities
│       ├── repository/         # Data access
│       └── service/            # Business logic
├── dashboard/                  # Vue 3 + TypeScript
│   ├── Dockerfile
│   ├── package.json
│   └── src/
│       ├── views/              # Status, Posts, Errors
│       ├── api/                # Backend client
│       └── App.vue             # Layout + routing
├── n8n-workflows/              # Exported workflow JSON
│   └── gaming-news-pipeline.json
└── docs/
    ├── architecture.md
    └── adding-sources.md
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/status` | Full system status + stats |
| GET | `/api/status/health` | Service health checks |
| POST | `/api/dedup/check` | Check/mark URL as seen |
| GET | `/api/dedup/count` | Number of tracked URLs |
| GET | `/api/posts` | Last 50 posted articles |
| POST | `/api/posts` | Log a posted article |
| GET | `/api/posts/stats` | Post count by source |
| GET | `/api/errors` | Last 100 errors |
| POST | `/api/errors` | Log an error |

## Adding a New Source

Create a YAML file in `sources/`:

```yaml
name: Ars Technica Gaming
type: rss
url: https://feeds.arstechnica.com/arstechnica/gaming
category: gaming
enabled: true
poll_interval_minutes: 30
```

Then add an RSS Read node in n8n, connect it to the Merge node, and add the domain to the source map in the dedup Code node.

## Telegram Output Example

```
#gaming

🎮 Starfield gets massive free update with new quest line and ship customization

Bethesda has released a major free update for Starfield adding a new
faction quest line spanning 8 missions and an overhauled ship builder.
The update also addresses over 200 reported bugs and adds DLSS 3 support.

https://www.pcgamer.com/starfield-free-update-2026/
```

## Skills Demonstrated

- **Java 25 + Spring Boot 4** — REST API, JPA, SQLite, service architecture
- **Vue 3 + TypeScript** — SPA with router, API client, reactive data
- **Container orchestration** — Multi-service Podman Compose stack
- **AI/LLM integration** — Local Ollama inference, prompt engineering
- **Workflow automation** — n8n with 8 RSS feeds, dedup, rate limiting
- **API integration** — Telegram Bot API, RSS/Atom, YouTube, GitHub
- **Linux / DevOps** — ROCm GPU acceleration, Nobara, shell scripts
- **Software architecture** — Plugin-based sources, clean separation of concerns

## License

MIT
