# Aristois Installer v1.1.0 — Guida Completa

## Panoramica

L'Aristois Installer è stato potenziato con:

- **Donor mode** sempre attivo (tutte le funzionalità sbloccate)
- **Seed Cracker migliorato** integrato (basato su fork di SeedCrackerX)
- **Anti-detection patching** per JAR mod (evita rilevamento anti-cheat)
- **Nessun download esterno** — tutto è già dentro l'installer

---

## Installazione

### Requisiti
- Java 8+ installato
- Minecraft Java Edition
- Una delle seguenti launcher: Vanilla, MultiMC, Prism

### Passaggi

1. **Avvia l'installer** — `java -jar Aristois-Donor.jar`

2. **Seleziona versione e launcher**
   - Scegli la versione Minecraft desiderata
   - Seleziona il launcher (Vanilla o MultiMC/Prism)

3. **Opzioni di installazione**
   - `Clean install` — rimuove versioni precedenti prima di installare
   - `Forge` — installa con supporto Forge (EMC Framework)
   - `Install SeedCrackerX` — **NUOVO** installa il seed cracker migliorato
   - `Anti-detection patch` — **NUOVO** offusca i metadata dei JAR

4. **Seleziona cartella launcher** — l'installer rileva automaticamente i path comuni

5. **Clicca Install** — il seed cracker viene copiato direttamente dall'installer

---

## Seed Cracker Migliorato

### Cosa c'è di diverso rispetto all'originale

| Feature | SeedCrackerX Originale | Versione Aristois |
|---------|----------------------|-------------------|
| Raccolta biomi | 16 punti per chunk | **256 punti per chunk** (16x) |
| Cracking da slime | ❌ Non supportato | ✅ **Slime chunk reversal** |
| Biome finder | Disabilitato default | **Abilitato default** |
| Download | Da GitHub (esterno) | **Embedded nell'installer** |
| Mixin Slime | ❌ | ✅ Rilevamento spawn automatico |

### Come Craccare un Seed

#### Metodo 1: Esplorazione libera (Slime Chunk) — NESSUNA STRUTTURA RICHIESTA

1. Entra in un mondo Minecraft
2. Esplora normalmente, preferibilmente in biomi palude o sottoterra (Y < 40)
3. Quando vedi uno **slime spawnare naturalmente**, il mod registra automaticamente quel chunk
4. I cuboid **blu** indicano chunk slime confermati (diversi dai cuboid verdi dei biomi)
5. Accumula **15-18 slime chunk** esplorando
6. Il seed viene craccato automaticamente quando ci sono abbastanza dati
7. Apri la GUI con `/seedcracker gui` per vedere il progresso

#### Metodo 2: Strutture (metodo classico)

1. Esplora e trova strutture: templi, piramidi, navi, monumenti, avamposti, igloo
2. I cuboid verdi indicano strutture rilevate
3. Servono **32 bit regolari + 40 bit liftable** per avviare il cracking
4. Il seed viene calcolato automaticamente

#### Metodo 3: End

1. Vai nell'End
2. I pilastri dell'End vengono analizzati automaticamente
3. Le città dell'End forniscono dati aggiuntivi

### Comandi

| Comando | Descrizione |
|---------|-------------|
| `/seedcracker gui` | Apre interfaccia configurazione |
| `/seedcracker data` | Mostra dati raccolti |
| `/seedcracker finder` | Stato finder attivi |
| `/seedcracker render` | Attiva/disattiva rendering cuboid |

---

## Anti-Detection

### Cosa fa

L'opzione "Anti-detection patch" (in SetupScene) modifica i JAR installati:

- **Modifica `fabric.mod.json`** — appende suffisso random all'ID mod
- **Modifica `mcmod.info`** — cambia nome visualizzato
- **Rinomina JAR** — nomi meno ovvi

Questo **NON garantisce** di bypassare tutti gli anti-cheat, ma rende più difficile il rilevamento tramite fingerprinting del mod ID.

### Limitazioni

- Funziona solo su JAR con metadata standard (fabric.mod.json / mcmod.info)
- Non modifica il bytecode della mod
- Plugin anti-cheat avanzati (Grim, Spartan) potrebbero ancora rilevare pattern di rete

---

## Struttura File Installati

### Installazione Vanilla (non-Forge)
```
.minecraft/
├── versions/
│   └── {versione}-Aristois/
│       ├── {versione}-Aristois.json
│       └── {versione}-Aristois.jar
├── mods/
│   └── Aristois-Seed-Cracker.jar    ← Seed Cracker migliorato
└── launcher_profiles.json           ← Profilo Aristois aggiunto
```

### Installazione Forge
```
.minecraft/
├── mods/
│   ├── Aristois-Seed-Cracker.jar    ← Seed Cracker
│   └── EMC.jar                      ← EMC Framework
└── libraries/
    └── EMC/
        └── {versione}/
            └── Aristois.jar         ← Aristois Mod
```

---

## Build da Sorgente

### Requisiti
- JDK 25+
- Git

### Compilazione Installer
```bash
cd Installer
./gradlew shadowJar
# Output: packager/Aristois-Donor.jar
```

### Compilazione Fork SeedCrackerX
```bash
cd SeedcrackerX-Fork
./gradlew build
# Output: build/libs/seedcrackerX-2.16.1.jar
```

### Aggiornamento Seed Cracker nell'Installer
```bash
cp SeedcrackerX-Fork/build/libs/seedcrackerX-2.16.1.jar \
   Installer/src/main/resources/aristois-seed-cracker.jar
cd Installer
./gradlew shadowJar
```

---

## Verifica Funzionamento

### Test Seed Cracker

1. Avvia installer, spunta "Install SeedCrackerX", installa
2. Avvia Minecraft con profilo Aristois
3. Apri chat: `/seedcracker gui`
4. Verifica tab "Slime Chunks" presente e attivo
5. Viaggia in una palude (o sottoterra Y<40)
6. Aspetta che spawni uno slime → dovresti vedere un cuboid blu
7. Il contatore slime chunk nella GUI dovrebbe aumentare

### Test Anti-Detection

1. Installa con "Anti-detection patch" attivo
2. Apri il JAR Aristois nella cartella versions con un tool zip
3. Verifica `fabric.mod.json` — `id` deve avere suffisso random
4. Avvia Minecraft — il mod deve caricarsi normalmente

---

## FAQ

**D: Perché il seed cracker non trova subito il seed?**
R: Servono abbastanza dati. Con slime chunk: 15-18 osservazioni. Con strutture: 32+40 bit. Continua a esplorare.

**D: Perché non vedo cuboid blu (slime chunk)?**
R: Devi essere in un bioma dove gli slime spawnano (palude, o sottoterra Y<40). Inoltre servono condizioni di luce corrette.

**D: Il seed cracker funziona su server?**
R: Sì, il seed cracking funziona su QUALSIASI server. Il server non può impedirti di osservare strutture e biomi — sono dati che il server DEVE inviarti per farti giocare.

**D: L'anti-detection mi protegge al 100%?**
R: No. Rende più difficile il rilevamento tramite fingerprint, ma anti-cheat sofisticati possono usare altri metodi (analisi pacchetti, pattern di movimento). È uno strato di protezione aggiuntivo, non una soluzione completa.

**D: Posso usare SeedCrackerX senza Aristois?**
R: Sì, il seed cracker è un mod Fabric standard. Funziona con qualsiasi installazione Fabric.

---

## Crediti

- **Aristois Installer** — Deftware & community Aristois
- **SeedCrackerX originale** — KaptainWutax, 19MisterX98
- **Migliorie Seed Cracker** — Biome denso, slime chunk cracking, Mixin fix
- **ChunkRandomReversal** — mjtb49
- **LattiCG** — seedfinding community

*Generato: Luglio 2026*
