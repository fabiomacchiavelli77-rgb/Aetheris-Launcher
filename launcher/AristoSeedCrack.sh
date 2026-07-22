#!/usr/bin/env bash
set -euo pipefail

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

JAR_PATH=""
if [ -f "$SCRIPT_DIR/Aristois-Donor.jar" ]; then
    JAR_PATH="$SCRIPT_DIR/Aristois-Donor.jar"
elif [ -f "$SCRIPT_DIR/../packager/Aristois-Donor.jar" ]; then
    JAR_PATH="$SCRIPT_DIR/../packager/Aristois-Donor.jar"
elif [ -f "$SCRIPT_DIR/packager/Aristois-Donor.jar" ]; then
    JAR_PATH="$SCRIPT_DIR/packager/Aristois-Donor.jar"
fi

if [ -z "$JAR_PATH" ]; then
    echo "[ERRORE] Aristois-Donor.jar non trovato."
    echo "Assicurati che il file JAR sia nella stessa cartella di questo script o nella cartella packager."
    exit 1
fi

java -jar "$JAR_PATH" "$@" || {
    EXIT_CODE=$?
    echo
    echo "[ERRORE] Installer terminato con errore (codice: $EXIT_CODE)" >&2
    exit $EXIT_CODE
}
