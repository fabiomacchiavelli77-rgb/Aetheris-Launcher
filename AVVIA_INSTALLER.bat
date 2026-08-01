@echo off
title Aetheris Installer
echo ==========================================
echo       Avvio di Aetheris Web Installer
echo ==========================================
echo.
cd /d "%~dp0Installer"
start "" "http://localhost:3000"
node server.js
pause
