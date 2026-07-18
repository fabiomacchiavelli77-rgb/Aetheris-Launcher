# Aristois Installer + SeedCrackerX Enhanced

**Aristois Installer v1.1.0** con Seed Cracker migliorato integrato e anti-detection patching.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Cosa include

| Modulo | Descrizione |
|--------|-------------|
| **Installer** | Installer desktop per Aristois mod (Minecraft) con donor mode sempre attivo |
| **SeedCrackerX** | Fork migliorato del seed cracker con slime chunk cracking (16x più denso) |
| **Anti-Detection** | Patching JAR automatico per offuscare metadata mod (fabric.mod.json / mcmod.info) |

## Novità v1.1.0

### Seed Cracker Migliorato
- **Slime Chunk Cracking** — cracca il seed senza bisogno di strutture, osservando solo slime spawn
- **256 punti/chunk** — raccolta biomi 16x più densa dell'originale (16 → 256)
- Biome finder abilitato di default
- Cracking automatico da 15-18 slime chunk
- Embedded nell'installer — nessun download esterno

### Anti-Detection
- Offusca `id` mod con suffisso random (6 char UUID)
- Compatibile Fabric e Forge
- Non modifica bytecode — solo metadata
- Opzionale: checkbox in SetupScene

## Requisiti

- Java 8+ (Installer)
- JDK 25+ (compilazione SeedCrackerX)
- Minecraft Java Edition
- Launcher: Vanilla, MultiMC, o Prism

## Build

### Installer
```bash
./gradlew shadowJar
# Output: packager/Aristois-Donor.jar
```

### SeedCrackerX
```bash
cd seedcracker
./gradlew build
# Output: seedcracker/build/libs/seedcrackerX-2.16.1.jar
```

### Aggiornare Seed Cracker nell'Installer
```bash
cp seedcracker/build/libs/seedcrackerX-2.16.1.jar \
   src/main/resources/aristois-seed-cracker.jar
./gradlew shadowJar
```

## Utilizzo

1. Avvia l'installer: `java -jar Aristois-Donor.jar`
2. Seleziona versione Minecraft e launcher
3. Opzioni:
   - `Clean install` — rimuove versioni precedenti
   - `Forge` — supporto Forge (EMC Framework)
   - `Install SeedCrackerX` — seed cracker migliorato
   - `Anti-detection patch` — offusca metadata JAR
4. Scegli cartella launcher (auto-rilevata)
5. Clicca **Install**

## Comandi Seed Cracker

| Comando | Descrizione |
|---------|-------------|
| `/seedcracker gui` | Apre interfaccia configurazione |
| `/seedcracker data` | Mostra dati raccolti |
| `/seedcracker finder` | Stato finder attivi |
| `/seedcracker render` | Attiva/disattiva rendering cuboid |

## Metodi di Cracking

### Slime Chunk (nessuna struttura richiesta)
Esplora in biomi palude o sottoterra (Y < 40). Quando uno slime spawna, il chunk viene registrato automaticamente. Cuboid **blu** = slime chunk confermato. Servono 15-18 osservazioni.

### Strutture (metodo classico)
Trova templi, piramidi, monumenti, navi, avamposti, igloo. Cuboid **verdi** = strutture rilevate. Servono 32 bit regolari + 40 bit liftable.

### End
I pilastri dell'End vengono analizzati automaticamente. Le città forniscono dati aggiuntivi.

## Struttura Progetto

```
├── src/                          ← Installer sorgente Java
│   └── main/
│       ├── java/me/deftware/installer/
│       │   ├── model/
│       │   │   ├── Configuration.java
│       │   │   ├── JarPatcher.java      ← Anti-detection patching
│       │   │   └── provider/
│       │   └── view/scenes/
│       │       ├── SetupScene.java      ← Checkbox SeedCrackerX + Anti-detection
│       │       ├── InstallingScene.java ← Wiring installazione
│       │       └── WelcomeScene.java
│       └── resources/
│           └── aristois-seed-cracker.jar  ← Seed Cracker embedded
├── seedcracker/                  ← Fork SeedCrackerX
│   └── src/main/java/kaptainwutax/seedcrackerX/
│       ├── cracker/
│       │   ├── SlimeChunkCracker.java   ← NUOVO
│       │   └── SlimeChunkData.java      ← NUOVO
│       ├── finder/
│       │   ├── SlimeChunkFinder.java    ← NUOVO
│       │   └── BiomeFinder.java         ← MODIFICATO (256 punti)
│       └── mixin/
│           └── SlimeEntityMixin.java    ← NUOVO
├── docs/
│   ├── GUIDA.md                  ← Guida completa
│   └── ROADMAP.md                ← Checklist sviluppo
└── packager/
    └── Aristois-Donor.jar        ← Build finale
```

## Crediti

- **Aristois Installer** — Deftware & community Aristois
- **SeedCrackerX originale** — KaptainWutax, 19MisterX98
- **ChunkRandomReversal** — mjtb49
- **LattiCG** — seedfinding community
- **Migliorie Seed Cracker + Anti-Detection** — Integrazione slime chunk cracking, biome finder denso, patching JAR automatico

## Licenza

MIT — vedi [LICENSE](LICENSE) per i dettagli.

Copyright (c) 2020 Aristois — Installer originale
Copyright (c) 2020 KaptainWutax — SeedCrackerX originale
