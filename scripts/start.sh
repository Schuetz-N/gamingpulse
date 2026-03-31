set -e
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "❌ .env nicht gefunden. Bitte .env.example kopieren und ausfüllen."
  exit 1
fi

ENV_PERMS=$(stat -c "%a" .env)
if [ "$ENV_PERMS" != "600" ]; then
  echo "⚠️  .env hat unsichere Berechtigungen ($ENV_PERMS). Setze auf 600..."
  chmod 600 .env
fi

source .env
if [ -z "$API_AUTH_TOKEN" ]; then
  echo "❌ API_AUTH_TOKEN ist nicht gesetzt. Bitte .env prüfen."
  exit 1
fi

echo "🚀 Starting GamingPulse stack..."
podman-compose up -d

sleep 3
DB_FILE=$(podman volume inspect gamingpulse_db_data --format '{{.Mountpoint}}')/gamingpulse.db
if [ -f "$DB_FILE" ]; then
  chmod 600 "$DB_FILE"
  echo "🔒 DB-Berechtigungen gesetzt (600)"
fi

echo "⏳ Waiting for n8n to be ready..."
until podman exec gamingpulse-n8n wget -qO- http://localhost:5678/healthz > /dev/null 2>&1; do
  sleep 3
done
echo "✅ n8n ready"

echo "⏳ Waiting for backend to be ready..."
until podman exec gamingpulse-backend curl -sf http://localhost:8080/api/status/health > /dev/null 2>&1; do
  sleep 3
done
echo "✅ Backend ready"

echo ""
echo "🔒 Kein Service ist direkt von außen erreichbar."
echo "   Zugriff nur via SSH-Tunnel:"
echo ""
echo "   Dashboard: ssh -L 3000:localhost:3000 user@dein-vps"
echo "              → http://localhost:3000"
echo ""
echo "   n8n:       ssh -L 5678:localhost:5678 user@dein-vps"
echo "              → http://localhost:5678"
echo ""
echo "GamingPulse is running. Stop with: ./scripts/stop.sh"