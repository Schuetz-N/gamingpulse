# GamingPulse 🎮

Automated gaming news pipeline. Collects content from gaming and tech sources, summarizes via local LLM (Ollama), and posts to a Telegram channel.

## Stack

- **Orchestration:** n8n (workflow automation)
- **LLM:** Ollama with ROCm (AMD GPU)
- **Backend:** Java 25 + Spring Boot 4
- **Dashboard:** Vue 3 + TypeScript
- **Database:** SQLite
- **Containers:** Podman

## Quick Start
```bash
cp .env.example .env
# Edit .env with your Telegram bot token and channel ID
./scripts/start.sh
```

## Architecture

See [docs/architecture.md](docs/architecture.md)

## Adding Sources

See [docs/adding-sources.md](docs/adding-sources.md)

## License

MIT
