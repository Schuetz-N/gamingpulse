#!/bin/bash
set -e
cd "$(dirname "$0")/.."

echo "🚀 Starting GamingPulse stack..."
podman-compose up -d

echo "⏳ Waiting for Ollama to be ready..."
until curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; do
  sleep 2
done
echo "✅ Ollama ready"

echo "⏳ Waiting for n8n to be ready..."
until curl -sf http://localhost:5678 > /dev/null 2>&1; do
  sleep 2
done
echo "✅ n8n ready"

echo ""
echo "📊 n8n:    http://localhost:5678"
echo "🤖 Ollama: http://localhost:11434"
echo ""
echo "GamingPulse is running. Stop with: ./scripts/stop.sh"
