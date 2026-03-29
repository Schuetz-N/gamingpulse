#!/bin/bash
cd "$(dirname "$0")/.."
echo "🛑 Stopping GamingPulse stack..."
podman-compose down
echo "✅ Stopped."
