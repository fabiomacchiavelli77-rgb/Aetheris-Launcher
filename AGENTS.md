# AGENTS.md

This file provides guidance to Gemini, Claude, Codex, and other AI agents when working with code in this repository.

> **Sync:** Keep in sync with `CLAUDE.md`. When this file changes, copy it to `CLAUDE.md`. When `CLAUDE.md` changes (Claude Code), copy it here.

## Project Overview

Aetheris Launcher — Minecraft 1.21.4 hacked client. Fork of Aristois Installer + SeedCrackerX, evolved into standalone Aetheris Client.

Three modules:
- **Root installer** (`src/`) — GLFW desktop app that installs Aristois into Minecraft launchers. Legacy. Builds `packager/Aristois-Donor.jar`.
- **Aetheris ClientCore** (`ClientCore/`) — Fabric mod with 32 hack modules + integrated SeedCrackerX. **Active development target. Fully migrated to 1.21.4 Mojang Official Mappings.**
- **Web installer** (`Installer/`) — Electron/Vite replacement for root installer. Express backend, vanilla JS frontend.

## Build Commands

### Root Installer (legacy)
```bash
./gradlew shadowJar          # → packager/Aristois-Donor.jar
```
Requires JDK 25. LWJGL 3.3.1 natives for win/mac/linux. Main class: `me.deftware.installer.Main`.

### Aetheris ClientCore (active)
```bash
cd ClientCore
./gradlew build              # → build/libs/aetheris-core-1.0.0.jar
./gradlew runClient          # test in dev environment
```
Minecraft 1.21.4, Fabric Loader 0.19.3, **Mojang official mappings**, Java 21. Uses Fabric Loom 1.9-SNAPSHOT.

Key dependencies: Fabric API 0.114.0, Cloth Config 15.0.140, seedfinding libraries (mc_math, mc_seed, mc_core, mc_noise, mc_biome, mc_terrain, mc_feature, mc_reversal), LattiCG 1.07.

### Web Installer
```bash
cd Installer
npm install
npm run dev                 # dev server
npm run build               # → dist/
node server.js              # production
```

## Architecture

### Aetheris ClientCore Module System (`ClientCore/src/main/java/net/aetheris/client/`)

All hacks extend `modules.Module` (abstract: `onEnable()`, `onDisable()`, `onTick()`). Registered in `modules.ModuleManager.init()`. Categories in `modules.Category` enum (COMBAT, MOVEMENT, RENDER, WORLD, PLAYER, SEEDCRACKER).

**Pattern for adding a module:**
1. Create class in `modules/impl/<category>/` extending `Module`
2. Register in `ModuleManager.init()`
3. If needs game hooks, create mixin in `mixins/` and register in `aetheris.mixins.json`

**Mixin architecture** — Active Aetheris mixins in `net.aetheris.client.mixins`:
- `MinecraftClientMixin` — module tick loop + Timer speed control
- `KeyboardMixin` — Right Shift toggles `AetherisMenuScreen`, Left Shift toggles `ClickGUI`
- `TitleScreenMixin` — watermark on title screen + Alt Manager button
- `PauseScreenMixin` — in-game Pause Menu quick buttons (Aetheris Menu, SeedCracker, Xray Ores, Alt Manager)
- `EntityMixin` — Velocity (cancel `setDeltaMovement`)
- `ClientPlayerEntityMixin` — NoFall (reset `fallDistance`)
- `ClientPlayerInteractionManagerMixin` — FastBreak (`destroyProgress` boost), Criticals (pre-attack push)
- `GameRendererMixin` — NoHurtCam (cancel `bobHurt`)
- `WorldRendererMixin` — ESP render hook (with 1.21.4 `GraphicsResourceAllocator`)
- `BlockRenderManagerMixin` — Xray block filter (cancel non-Xray `renderBatched`)
- `PlayerEntityMixin` — NoHunger (cancel `causeFoodExhaustion`)

*Note: `Reach` uses native Minecraft 1.21.4 `Attributes.ENTITY_INTERACTION_RANGE` & `Attributes.BLOCK_INTERACTION_RANGE` directly in `Reach.onTick()`, requiring no Mixin.*

### Integrated SeedCrackerX (`ClientCore/src/main/java/kaptainwutax/seedcrackerX/`)

74 Java files copied from the original standalone mod. 4 mixins (`ClientLevelMixin`, `ClientPacketListenerMixin`, `LocalPlayerMixin`, `SlimeEntityMixin`) in `kaptainwutax.seedcrackerX.mixin`. Entrypoint at `SeedCracker.java` (implements `ModInitializer`, registered in `fabric.mod.json`). Adds `/seedcracker` chat commands. Native configuration screen at `net.aetheris.client.gui.SeedCrackerConfigScreen`.

**Dual mixin configs:** `aetheris.mixins.json` (Aetheris) + `seedcracker.mixins.json` (SeedCrackerX). Both listed in `fabric.mod.json`.

**Dual entrypoints:** `AetherisClient` (client) + `SeedCracker` (main). Both in `fabric.mod.json`.

### Web Installer Architecture (`Installer/`)

Express backend (`server.js`) + Vite frontend. Automatically installs Fabric 0.19.3 for MC 1.21.4, creates custom `Aetheris-1.21.4` profile, and copies `aetheris-core-1.0.0.jar` into `.minecraft/mods/`.
`server.js` resolves `sourceJar` path dynamically relative to `__dirname` (`path.resolve(__dirname, '../ClientCore/build/libs/aetheris-core-1.0.0.jar')`).

### Mapping Critical Note

**The entire ClientCore uses Mojang official mappings** (`loom.officialMojangMappings()` in build.gradle). Switched from Yarn to unify with SeedCrackerX. Key class names & changes in 1.21.4:
- `Minecraft` (not `MinecraftClient`)
- `LocalPlayer` (not `ClientPlayerEntity`)
- `MultiPlayerGameMode` (not `ClientPlayerInteractionManager`)
- `mc.level` (not `mc.world`)
- `mc.gameMode` (not `mc.interactionManager`)
- `mc.hitResult` (not `mc.crosshairTarget`)
- `mc.options` (same)
- `mc.player.getDeltaMovement()` (not `getVelocity()`)
- `mc.player.onGround()` (field `onGround`, not method)
- `mc.player.getAttackStrengthScale()` (not `getAttackCooldownProgress()`)
- `InteractionHand` (not `Hand`)
- Armor defense uses `DataComponents.EQUIPPABLE` and `Attributes.ARMOR` via `DataComponents.ATTRIBUTE_MODIFIERS`.
- Block colors use explicit constants (e.g. `Blocks.BLUE_TERRACOTTA`, `Blocks.WAXED_COPPER_BULB`).
- `fabric.mod.json` `depends` block specifies only `fabricloader`, `minecraft`, and `java` to prevent sub-module dependency conflicts.

## Keybind System

Each module has a `keybind` field (int, GLFW code). Default `GLFW.GLFW_KEY_UNKNOWN` (-1 = no key).
- **Shift+Click** on a module in the menu opens keybind recording. Press key to assign, ESC to clear.
- Dedicated `KeybindManagerScreen` provides a searchable list to bind/unbind module keys.
- `KeyboardMixin` intercepts keys: if no screen is open, toggles module.
- Left Shift opens **ClickGUI**, Right Shift opens Classic Menu.

## Profile System

`config/ProfileManager` saves/loads state and keybinds to `.minecraft/aetheris/profile.json`.
- Auto-saves on state change.
- Loaded on client initialization.

## GUI System

Multiple GUIs available:
- **PauseScreen Quick Buttons** (Pause Menu) — Quick access to Aetheris Menu, SeedCracker Config, Xray Ores, Alt Manager.
- **AetherisMenuScreen** (Right Shift) — Simple category list with scrolling, toggle, keybind assignment, and bottom quick navigation bar.
- **ClickGUI** (Left Shift) — Aristois/Wurst/Meteor-inspired premium GUI. Horizontal category columns side-by-side (Combat, Movement, Render, World, Player, SeedCracker). Each column has a colored accent header bar, plain-text module rows with left accent bar when enabled, hover overlay, right-click inline dropdown (keybind + settings), per-column scrolling, real-time search bar (bottom center), fade-in animation, and module count badges. Colors: Combat=Red, Movement=Green, Render=Cyan, World=Amber, Player=Violet, SeedCracker=Teal.
- **AltManagerScreen** — Offline account profile switcher for dynamic username changes.
- **XrayBlockSelectorScreen** — Full Minecraft block registry selector with 3-column layout (block icon, name EN/IT, checkbox), search/filter, and buttons for Default/Clear All/Select All.
- **KeybindManagerScreen** — Searchable keybind manager for all 32 modules.
- **SeedCrackerConfigScreen** — Native SeedCrackerX config and seed display GUI.

## Key File Locations

| Purpose | Path |
|---------|------|
| Module base class | `ClientCore/.../modules/Module.java` |
| Module registry | `ClientCore/.../modules/ModuleManager.java` |
| Profile manager | `ClientCore/.../config/ProfileManager.java` |
| Simple GUI | `ClientCore/.../gui/AetherisMenuScreen.java` |
| ClickGUI | `ClientCore/.../gui/ClickGUI.java` |
| Alt Manager GUI | `ClientCore/.../gui/AltManagerScreen.java` |
| Xray Ore Selector GUI | `ClientCore/.../gui/XrayBlockSelectorScreen.java` |
| Keybind Manager GUI | `ClientCore/.../gui/KeybindManagerScreen.java` |
| SeedCracker Config GUI | `ClientCore/.../gui/SeedCrackerConfigScreen.java` |
| Pause Menu Mixin | `ClientCore/.../mixins/PauseScreenMixin.java` |
| Client entrypoint | `ClientCore/.../AetherisClient.java` |
| Aetheris mixin config | `ClientCore/src/main/resources/aetheris.mixins.json` |
| SeedCracker mixin config | `ClientCore/src/main/resources/seedcracker.mixins.json` |
| Fabric mod manifest | `ClientCore/src/main/resources/fabric.mod.json` |
| Web Installer backend | `Installer/server.js` |
| Root installer entry | `src/main/java/me/deftware/installer/Main.java` |

## Module List (32 total)

**Combat (8):** KillAura, Velocity, Criticals, Reach, AutoArmor, AutoTotem, TriggerBot, Surround
**Movement (7):** AutoSprint, Speed, Fly, NoFall, Step, NoSlowdown, NoClip
**Render (7):** FullBright, ESP, NoHurtCam, Xray, NameTags, Tracers, FreeCam
**World (4):** FastBreak, Scaffold, Timer, AutoTool
**Player (6):** AutoRespawn, FastPlace, NoHunger, ChestStealer, AutoFish, InventoryCleaner

