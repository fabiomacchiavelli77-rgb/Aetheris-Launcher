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

:: Determina il percorso corretto del JAR (stessa cartella, ../packager, o packager/)
set "JAR_PATH="
if exist "%~dp0Aristois-Donor.jar" (
    set "JAR_PATH=%~dp0Aristois-Donor.jar"
) else if exist "%~dp0..\packager\Aristois-Donor.jar" (
    set "JAR_PATH=%~dp0..\packager\Aristois-Donor.jar"
) else if exist "%~dp0packager\Aristois-Donor.jar" (
    set "JAR_PATH=%~dp0packager\Aristois-Donor.jar"
)

if "%JAR_PATH%"=="" (
    echo [ERRORE] Aristois-Donor.jar non trovato.
    echo Assicurati che il file JAR sia nella stessa cartella di questo script o nella cartella packager.
    pause
    exit /b 1
)

:: Avvia l'installer
"%JAVA%" -jar "%JAR_PATH%" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRORE] Installer terminato con errore
    pause
)
