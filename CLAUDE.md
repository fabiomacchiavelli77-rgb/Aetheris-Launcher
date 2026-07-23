# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Sync:** Keep in sync with `AGENTS.md`. When this file changes, copy it to `AGENTS.md`. When `AGENTS.md` changes (Gemini), copy it here.

## Project Overview

Aetheris Launcher — Minecraft 1.21.4 hacked client. Fork of Aristois Installer + SeedCrackerX, evolved into standalone Aetheris Client.

Three modules:
- **Root installer** (`src/`) — GLFW desktop app that installs Aristois into Minecraft launchers. Legacy. Builds `packager/Aristois-Donor.jar`.
- **Aetheris ClientCore** (`NuovoFork/ClientCore/`) — Fabric mod with 32 hack modules + integrated SeedCrackerX. **Active development target.**
- **Web installer** (`NuovoFork/Installer/`) — Electron/Vite replacement for root installer. Express backend, vanilla JS frontend.

## Build Commands

### Root Installer (legacy)
```bash
./gradlew shadowJar          # → packager/Aristois-Donor.jar
```
Requires JDK 25. LWJGL 3.3.1 natives for win/mac/linux. Main class: `me.deftware.installer.Main`.

### Aetheris ClientCore (active)
```bash
cd NuovoFork/ClientCore
./gradlew build              # → build/libs/aetheris-core-1.0.0.jar
./gradlew runClient          # test in dev environment
```
Minecraft 1.21.4, Fabric Loader 0.19.3, **Mojang official mappings**, Java 21. Uses Fabric Loom 1.9-SNAPSHOT.

Key dependencies: Fabric API 0.114.0, Cloth Config 15.0.140, seedfinding libraries (mc_math, mc_seed, mc_core, mc_noise, mc_biome, mc_terrain, mc_feature, mc_reversal), LattiCG 1.07.

### Web Installer
```bash
cd NuovoFork/Installer
npm install
npm run dev                 # dev server
npm run build               # → dist/
node server.js              # production
```

## Architecture

### Aetheris ClientCore Module System (`NuovoFork/ClientCore/src/main/java/net/aetheris/client/`)

All hacks extend `modules.Module` (abstract: `onEnable()`, `onDisable()`, `onTick()`). Registered in `modules.ModuleManager.init()`. Categories in `modules.Category` enum (COMBAT, MOVEMENT, RENDER, WORLD, PLAYER, SEEDCRACKER).

**Pattern for adding a module:**
1. Create class in `modules/impl/<category>/` extending `Module`
2. Register in `ModuleManager.init()`
3. If needs game hooks, create mixin in `mixins/` and register in `aetheris.mixins.json`

**Mixin architecture** — 8 Aetheris mixins in `net.aetheris.client.mixins`:
- `MinecraftClientMixin` — module tick loop + Timer speed control
- `KeyboardMixin` — Right Shift toggles `AetherisMenuScreen`
- `TitleScreenMixin` — watermark on title screen
- `ClientPlayerEntityMixin` — Velocity (cancel `lerpMotion`) + NoFall (reset fallDistance)
- `ClientPlayerInteractionManagerMixin` — Reach (`getPickRange`), FastBreak (`destroyProgress` boost), Criticals (pre-attack push)
- `GameRendererMixin` — NoHurtCam (cancel `bobHurt`)
- `WorldRendererMixin` — ESP render hook
- `BlockRenderManagerMixin` — Xray block filter (cancel non-Xray `renderBatched`)
- `LivingEntityMixin` — Criticals onGround spoof
- `PlayerEntityMixin` — NoHunger (cancel `causeFoodExhaustion`)

### Integrated SeedCrackerX (`NuovoFork/ClientCore/src/main/java/kaptainwutax/seedcrackerX/`)

74 Java files copied from the original standalone mod. 4 mixins (`ClientLevelMixin`, `ClientPacketListenerMixin`, `LocalPlayerMixin`, `SlimeEntityMixin`) in `kaptainwutax.seedcrackerX.mixin`. Entrypoint at `SeedCracker.java` (implements `ModInitializer`, registered in `fabric.mod.json`). Adds `/seedcracker` chat commands.

**Dual mixin configs:** `aetheris.mixins.json` (Aetheris) + `seedcracker.mixins.json` (SeedCrackerX). Both listed in `fabric.mod.json`.

**Dual entrypoints:** `AetherisClient` (client) + `SeedCracker` (main). Both in `fabric.mod.json`.

### Root Installer Architecture

GLFW + NanoVG UI. Scene-based navigation (`view/scenes/`). Providers abstract launcher-specific install logic (`model/launcher/`). `JarPatcher.java` handles anti-detection metadata obfuscation. Target Java 8 bytecode, builds via Shadow jar plugin with all natives bundled.

### Mapping Critical Note

**The entire ClientCore uses Mojang official mappings** (`loom.officialMojangMappings()` in build.gradle). Switched from Yarn to unify with SeedCrackerX. Key class names:
- `Minecraft` (not `MinecraftClient`)
- `LocalPlayer` (not `ClientPlayerEntity`)
- `MultiPlayerGameMode` (not `ClientPlayerInteractionManager`)
- `mc.level` (not `mc.world`)
- `mc.gameMode` (not `mc.interactionManager`)
- `mc.hitResult` (not `mc.crosshairTarget`)
- `mc.options` (same)
- `mc.player.getDeltaMovement()` (not `getVelocity()`)
- `mc.player.onGround()` (field, not method)
- `mc.player.getAttackStrengthScale()` (not `getAttackCooldownProgress()`)
- `InteractionHand` (not `Hand`)

## Keybind System

Each module ha un campo `keybind` (int, codice GLFW). Default `GLFW.GLFW_KEY_UNKNOWN` (-1 = nessun tasto).
- **Shift+Click** su un modulo nel menu apre la registrazione keybind. Premi un tasto per assegnarlo, ESC per rimuoverlo.
- `KeyboardMixin` intercetta i tasti: se nessun menu è aperto e il tasto corrisponde a un keybind, il modulo viene toggleato.
- Left Shift apre la **ClickGUI**, Right Shift apre il menu classico.

## Profile System

`config/ProfileManager` salva/carica stato e keybind dei moduli in `.minecraft/aetheris/profile.json`.
- Auto-save a ogni toggle, attivazione, disattivazione.
- Caricato all'avvio in `AetherisClient.onInitializeClient()`.

## GUI System

Due GUI disponibili:
- **AetherisMenuScreen** (Right Shift) — lista semplice per categoria con scroll, toggle, registrazione keybind
- **ClickGUI** (Left Shift) — pannelli trascinabili, colorati per categoria, toggle click, collapse/expand categorie

## Key File Locations

| Purpose | Path |
|---------|------|
| Module base class | `NuovoFork/ClientCore/.../modules/Module.java` |
| Module registry | `NuovoFork/ClientCore/.../modules/ModuleManager.java` |
| Profile manager | `NuovoFork/ClientCore/.../config/ProfileManager.java` |
| Simple GUI | `NuovoFork/ClientCore/.../gui/AetherisMenuScreen.java` |
| ClickGUI | `NuovoFork/ClientCore/.../gui/ClickGUI.java` |
| Client entrypoint | `NuovoFork/ClientCore/.../AetherisClient.java` |
| Aetheris mixin config | `NuovoFork/ClientCore/src/main/resources/aetheris.mixins.json` |
| SeedCracker mixin config | `NuovoFork/ClientCore/src/main/resources/seedcracker.mixins.json` |
| Fabric mod manifest | `NuovoFork/ClientCore/src/main/resources/fabric.mod.json` |
| Access widener | `NuovoFork/ClientCore/src/main/resources/aetheris.accesswidener` |
| Root installer entry | `src/main/java/me/deftware/installer/Main.java` |
| Installer config | `src/main/resources/config.json` |
| Donor mode toggle | `build.gradle` → `donorbuild` property |
| Launcher scripts | `launcher/AristoSeedCrack.bat`, `launcher/AristoSeedCrack.sh` |

## Module List (32 total)

**Combat (8):** KillAura, Velocity, Criticals, Reach, AutoArmor, AutoTotem, TriggerBot, Surround
**Movement (7):** AutoSprint, Speed, Fly, NoFall, Step, NoSlowdown, NoClip
**Render (7):** FullBright, ESP, NoHurtCam, Xray, NameTags, Tracers, FreeCam
**World (4):** FastBreak, Scaffold, Timer, AutoTool
**Player (6):** AutoRespawn, FastPlace, NoHunger, ChestStealer, AutoFish, InventoryCleaner
