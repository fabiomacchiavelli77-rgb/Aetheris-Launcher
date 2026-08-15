# Aetheris Client & Web Installer (Minecraft 1.21.11 / 26.2)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net)
[![Fabric Loader](https://img.shields.io/badge/Fabric-0.19.3-blue.svg)](https://fabricmc.net)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Aetheris Client** è un hacked client autonomo per Minecraft **1.21.11 (26.2)** basato su Fabric Loader e Mojang Official Mappings. Include **66 moduli di hack**, **SeedCrackerX integrato** nativamente e un **Web Installer** moderno per l'installazione automatica.

---

## 🌟 Caratteristiche Principali

- **Full Native 1.21.11 (26.2)**: Totalmente compilato con **Mojang Official Mappings** e Java 21.
- **66 Moduli Hack**:
  - **Combat**: KillAura, CrystalAura (con auto-placement), Velocity, BowAimbot (con calcolo anticipo), BedAura, Reach, TriggerBot, Surround, AimAssist, ecc.
  - **Movement**: AutoSprint, Speed, Fly, NoFall, BunnyJump, Jetpack, Step, NoSlowdown, NoClip, AutoWalk, Sneak, Gesu (cammina sull'acqua).
  - **Render**: FullBright, ESP, StorageESP (con filtro vuote), Xray, Tracers, NameTags, FreeCam, CameraClip, Trajectories, Waypoints (marcatura 3D).
  - **World**: FastBreak, Scaffold, Timer, LiquidInteract, AutoSign, AutoFarm, AirPlace, AutoBrewer, AutoSmelter, StrongholdFinder, PluginScanner, PacketLogger, ServerFinder.
  - **Player**: AutoEat, AutoFish, InventoryCleaner, AntiAFK, InventorySort, AntiDetect, NoChatReports, AutoRespawn, FastPlace, NoHunger, ChestStealer, PermissionViewer.
  - **SeedCracker**: Modulo dedicato con interfaccia nativa per SeedCrackerX.
- **SeedCrackerX Integrato**: Seed cracking nativo in-game con comandi `/seedcracker` e menu dedicato senza mod esterne.
- **Interfaccia Grafica Doppia**:
  - **ClickGUI** (`Right Shift`): Interfaccia moderna responsive stile Aristois/Wurst/Meteor con colonne trascinabili, ricerca, drawer impostazioni e z-index layering dinamico.
  - **Aetheris Menu** (`Right Ctrl` + `Right Shift`): Menu classico a categorie rapide con navigazione rapida e Keybind Manager.
  - **Pause Menu**: Bottoni di avvio rapido integrati nel menu di pausa di Minecraft.
- **Bypass & Stealth**:
  - **AntiDetect**: Spoof del brand client (`vanilla`) e blocco dei pacchetti `fabric:*`.
  - **NoChatReports**: Stripping delle firme digitali per prevenire report su server 1.21+.
  - **PluginScanner**: Rilevamento passivo dei plugin server via dati pubblici (brand, plugin channels, tab-complete) con report categorizzato (Permessi, Anti-cheat, World-edit, Economia).

---

## 🛠️ Requisiti di Sistema

- **Minecraft Java Edition**: Versioni supportate fino a 1.21.11 (26.2).
- **Java**: JDK 21 (o superiore).
- **Node.js**: v18+ (necessario per avviare o compilare il Web Installer).

---

## 🚀 Guida Rapida / Installazione

### Metodo 1: Avvio tramite Web Installer (Consigliato)

1. Scarica/clona la repository.
2. Esegui il file batch principale:
   ```cmd
   AVVIA_INSTALLER.bat
   ```
3. Nel browser si aprirà l'interfaccia dell'installer su `http://localhost:3000`.
4. Clicca **Install Aetheris** per installare automaticamente il profilo `Aetheris-1.21.11` e il JAR nella cartella `.minecraft`.

### Metodo 2: Compilazione Manuale (Developer Mode)

```bash
# Entra nella cartella del client core
cd ClientCore

# Compila il JAR (.jar prodotto in build/libs/aetheris-core-1.0.0.jar)
./gradlew build

# Testa direttamente in ambiente dev
./gradlew runClient
```

Una volta compilato, copia `build/libs/aetheris-core-1.0.0.jar` all'interno della cartella `.minecraft/mods/` con Fabric Loader 0.19.3 per 1.21.11.

---

## 🎮 Controlli & Keybinds

| Azione | Keybind Predefinito |
|--------|---------------------|
| **Apri ClickGUI** (Nuova GUI) | `Right Shift` |
| **Apri Aetheris Menu** (Vecchia GUI) | `Right Ctrl` + `Right Shift` |
| **Imposta Keybind su Modulo** | `Shift` + Click sul modulo nel menu |
| **Pause Menu Quick Bar** | `ESC` in gioco ➔ Bottoni rapidi in alto |

---

## 🛡️ Server Audit (per amministratori)

Nella cartella `tools/` è incluso **`aetheris_server_audit.py`**, un tool difensivo per chi **gestisce un proprio server Minecraft**: analizza localmente `server.properties`, `ops.json` e le configurazioni di **LuckPerms/PermissionsEx** per segnalare permessi wildcard pericolosi, ereditarietà rischiose, RCON debole e altre misconfigurazioni.

```bash
python tools/aetheris_server_audit.py /path/alla/cartella/server --md report.md
```

È un analizzatore **puramente locale** dei propri file: non esegue scanning remoto di server di terzi. Dettagli in [`tools/README.md`](tools/README.md).

---

## 📁 Struttura Progetto

```
Aetheris/
├── ClientCore/               ← Core Mod Client (Fabric 1.21.11, Java 21)
│   ├── src/main/java/net/aetheris/client/
│   │   ├── modules/          ← 66 Moduli Hack divisi per Categoria
│   │   ├── gui/              ← ClickGUI, AetherisMenu, Xray, AltManager, etc.
│   │   ├── mixins/           ← Mixin di sistema (Render, Network, Input)
│   │   └── config/           ← Gestione profilo utente (profile.json)
│   └── src/main/java/kaptainwutax/seedcrackerX/ ← SeedCrackerX integrato
├── Installer/                ← Web Installer (Express + Vite JS)
│   ├── server.js             ← Backend Express per installazione silente Fabric
│   └── dist/                 ← Frontend statico compilato dell'installer
├── tools/                    ← Tool difensivi per admin (aetheris_server_audit.py)
├── AVVIA_INSTALLER.bat       ← Launcher script rapido per Windows
├── AGENTS.md / CLAUDE.md     ← Documentazione per sviluppatori e AI Agents
└── Aetheris_Checklist_Test.xlsx ← Excel di tracking per i test e verifica moduli
```

---

## 📄 Licenza

Questo progetto è distribuito sotto licenza **MIT**. Consulta il file [LICENSE](LICENSE) per i dettagli.
