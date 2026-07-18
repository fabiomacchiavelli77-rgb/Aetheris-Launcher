#!/usr/bin/env bash
set -e

echo "========================================"
echo "  AristoSeedCrack v1.1.0"
echo "  Aristois Installer + SeedCrackerX"
echo "========================================"
echo

# Trova Java
if ! command -v java &>/dev/null; then
    echo "[ERRORE] Java non trovato nel PATH"
    echo "Installa Java 8+ con: sudo apt install openjdk-21-jre"
    exit 1
fi

# Avvia l'installer
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
java -jar "$SCRIPT_DIR/packager/Aristois-Donor.jar" "$@"
