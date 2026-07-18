# Roadmap Checklist — Aristois Installer v1.1.0

## Fase 1: Fondamenta (file indipendenti, nessun impatto UI)
- [x] **Configuration.java** — Aggiungere campi `seedCracker`, `antiDetection` con builder methods
- [x] **CheckBox.java** — Aggiungere callback `onChange` con setter `withOnChange(Runnable)`
- [x] **JarPatcher.java** — Creare utility per patchare metadata JAR (fabric.mod.json, mcmod.info)
- [x] **SeedCrackerManager.java** — Creare singleton: fetch releases GitHub, match versioni, download JAR

## Fase 2: Provider Interface
- [x] **Provider.java** — Aggiungere `installSeedCracker()` default method

## Fase 3: UI e Wiring
- [x] **SetupScene.java** — Aggiungere checkbox "Install SeedCrackerX" e "Anti-detection patch" con interdipendenza Forge
- [x] **WelcomeScene.java** — Aggiungere fetch releases SeedCrackerX in `fetch()` (non-critico)
- [x] **InstallingScene.java** — Aggiungere chiamata `installSeedCracker()` e loop patching anti-detection in `run()`

## Fase 4: Build e Verifica
- [x] `./gradlew compileJava` — Compilazione pulita ✓
- [x] `./gradlew shadowJar` — Build JAR completo ✓
- [ ] Test manuale Seed Cracker: installare con checkbox, verificare `SeedCrackerX.jar` nella cartella mods
- [ ] Test manuale Anti-Detection: installare con checkbox, aprire JAR, verificare `fabric.mod.json` patchato
- [ ] Test manuale Forge: verificare che Forge + SeedCrackerX mostri warning e salti installazione
- [ ] Test manuale rete assente: verificare che fallimento non blocchi installazione principale
