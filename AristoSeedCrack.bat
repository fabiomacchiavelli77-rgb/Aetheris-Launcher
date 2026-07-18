@echo off
title AristoSeedCrack v1.1.0
echo ========================================
echo   AristoSeedCrack v1.1.0
echo   Aristois Installer + SeedCrackerX
echo ========================================
echo.
echo Avvio installer...
echo.

:: Trova Java
set JAVA=java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRORE] Java non trovato nel PATH
    echo Installa Java 8+ da https://adoptium.net
    pause
    exit /b 1
)

:: Avvia l'installer
"%JAVA%" -jar "%~dp0packager\Aristois-Donor.jar" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRORE] Installer terminato con errore
    pause
)
