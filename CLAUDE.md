# AGENTS.md

This file provides guidance to Gemini, Claude, Codex, and other AI agents when working with code in this repository.

> **Sync:** Keep in sync with `CLAUDE.md`. When this file changes, copy it to `CLAUDE.md`. When `CLAUDE.md` changes (Claude Code), copy it here.

## Project Overview

Aetheris Launcher — Minecraft 1.21.11 / 26.2 hacked client. Fork of Aristois Installer + SeedCrackerX, evolved into standalone Aetheris Client.

Three modules:
- **Root installer** (`src/`) — GLFW desktop app that installs Aristois into Minecraft launchers. Legacy. Builds `packager/Aristois-Donor.jar`.
- **Aetheris ClientCore** (`ClientCore/`) — Fabric mod with 62 hack modules + integrated SeedCrackerX. **Active development target. Fully migrated to 1.21.11 (26.2) Mojang Official Mappings.**
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
Minecraft 1.21.11 (26.2), Fabric Loader 0.19.3, **Mojang official mappings**, Java 21. Uses Fabric Loom 1.17.19.

Key dependencies: Fabric API 0.141.6+1.21.11, Cloth Config 15.0.140, seedfinding libraries (mc_math, mc_seed, mc_core, mc_noise, mc_biome, mc_terrain, mc_feature, mc_reversal), LattiCG 1.07.

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
- `ModuleManager` mantiene un indice `Class -> Module` (`ConcurrentHashMap`): i mixin hot-path (render, tick, rete) usano `ModuleManager.getModule(X.class)` O(1), NON loop su `getModules()`. I loop sono ammessi solo su eventi cold (screen open, place item).
- `KeyboardMixin` — either Shift toggles `ClickGUI`; module keybinds work only while no screen is open.
- `TitleScreenMixin` — watermark on title screen + Alt Manager button
- `PauseScreenMixin` — in-game Pause Menu quick buttons (Aetheris Menu, SeedCracker, Xray Ores, Alt Manager)
- `EntityMixin` — Velocity (cancel `setDeltaMovement`)
- `ClientPlayerEntityMixin` — NoFall (reset `fallDistance`)
- `ClientPlayerInteractionManagerMixin` — FastBreak (`destroyProgress` boost), Criticals (pre-attack push)
- `GameRendererMixin` — NoHurtCam (cancel `bobHurt`), renderLevel null check
- `WorldRendererMixin` — ESP render hook (with 1.21.4 `GraphicsResourceAllocator`)
- `BlockRenderManagerMixin` — Xray block filter (cancel non-Xray `renderBatched`)
- `PlayerEntityMixin` — NoHunger (cancel `causeFoodExhaustion`)
- `BlockItemMixin` — LiquidInteract (override `canPlace` per piazzare su liquidi), AirPlace
- `AutoSignMixin` — AutoSign (Accessor/Invoker su `AbstractSignEditScreen`: scrive `messages[]`+`text`, chiama `onDone`)
- `StorageESPMixin` — StorageESP (hook su `LevelRenderer.renderLevel` TAIL, box `RenderType.lines()`)
- `AntiDetectMixin` — AntiDetect (hook su `Connection.send`: spoofa `BrandPayload` → "vanilla" e cancella payload `fabric:*`)
- `ChatSignatureMixin` — NoChatReports (strip `MessageSignature` da `ServerboundChatPacket`/downgrade `ServerboundChatCommandSignedPacket` → `ServerboundChatCommandPacket` per bypassare plugin anti-hack che tracciano comandi firmati)
- `ConnectionMixin` — PacketLogger outbound+inbound (`send` + `channelRead0`), FreeCam (cancella move/interact packet), NoFall (riscrive `ServerboundMovePlayerPacket` con `onGround=true`). Moduli cached via `ModuleManager.getModule(Class)` per performance.
- `CameraMixin` — CameraClip (override `getMaxZoom`)
- `AbstractFurnaceMenuAccessor` — AutoSmelter (accessor for furnace slot access)
- `DiscardedPayloadMixin` — PluginScanner (sniffa i canali registrati dal server via `minecraft:register`)

*Note: `Reach` uses native Minecraft 1.21.4 `Attributes.ENTITY_INTERACTION_RANGE` & `Attributes.BLOCK_INTERACTION_RANGE` directly in `Reach.onTick()`, requiring no Mixin.*

### Integrated SeedCrackerX (`ClientCore/src/main/java/kaptainwutax/seedcrackerX/`)

74 Java files copied from the original standalone mod. 4 mixins (`ClientLevelMixin`, `ClientPacketListenerMixin`, `LocalPlayerMixin`, `SlimeEntityMixin`) in `kaptainwutax.seedcrackerX.mixin`. Entrypoint at `SeedCracker.java` (implements `ModInitializer`, registered in `fabric.mod.json`). Adds `/seedcracker` chat commands. Native configuration screen at `net.aetheris.client.gui.SeedCrackerConfigScreen`.

**Dual mixin configs:** `aetheris.mixins.json` (Aetheris) + `seedcracker.mixins.json` (SeedCrackerX). Both listed in `fabric.mod.json`.

**Dual entrypoints:** `AetherisClient` (client) + `SeedCracker` (main). Both in `fabric.mod.json`.

### Web Installer Architecture (`Installer/`)

Express backend (`server.js`) + Vite frontend. Automatically installs Fabric 0.19.3 for MC 1.21.11, creates custom `Aetheris-1.21.11` profile, and copies `aetheris-core-1.0.0.jar` into `.minecraft/mods/`.
`server.js` resolves `sourceJar` path dynamically relative to `__dirname` (`path.resolve(__dirname, '../ClientCore/build/libs/aetheris-core-1.0.0.jar')`).

### Mapping & 1.21.11 API Critical Note

**The entire ClientCore uses Mojang official mappings** (`loom.officialMojangMappings()` in build.gradle). Switched from Yarn to unify with SeedCrackerX. Key class names & changes in 1.21.11 (26.2):
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
- **Screen Input Events (1.21.11)**:
  - `mouseClicked(MouseButtonEvent event, boolean isDoubleClick)` (was `mouseClicked(double, double, int)`)
  - `mouseReleased(MouseButtonEvent event)` (was `mouseReleased(double, double, int)`)
  - `mouseDragged(MouseButtonEvent event, double deltaX, double deltaY)` (was `mouseDragged(double, double, int, double, double)`)
  - `keyPressed(KeyEvent event)` (was `keyPressed(int, int, int)`)
  - `charTyped(CharacterEvent event)` (was `charTyped(char, int)`)
- **Rendering & Mixins (1.21.11)**:
  - `KeyboardHandler.keyPress`: takes `(long, int, KeyEvent)`
  - `BlockRenderDispatcher.renderBatched`: uses `List<?>` parameter instead of `RandomSource`
  - `LevelRenderer`: `collectVisibleEntities` replaced by `extractVisibleEntities(Camera, Frustum, DeltaTracker, LevelRenderState)` populating `state.entityRenderStates`
  - `LevelRenderer.renderLevel`: updated parameters signature (no `GameRenderer` parameter)
- Armor defense uses `DataComponents.EQUIPPABLE` and `Attributes.ARMOR` via `DataComponents.ATTRIBUTE_MODIFIERS`.
- Block colors use explicit constants (e.g. `Blocks.BLUE_TERRACOTTA`, `Blocks.WAXED_COPPER_BULB`).
- `fabric.mod.json` `depends` block specifies only `fabricloader`, `minecraft`, and `java` to prevent sub-module dependency conflicts.

## Keybind System

Each module has a `keybind` field (int, GLFW code). Default `GLFW.GLFW_KEY_UNKNOWN` (-1 = no key).
- **Shift+Click** on a module in the menu opens keybind recording. Press key to assign, ESC to clear.
- Dedicated `KeybindManagerScreen` provides a searchable list to bind/unbind module keys.
- `KeyboardMixin` intercepts keys: if no screen is open, toggles module.
- **Right Shift** opens **ClickGUI** (modern GUI).
- **Right Ctrl + Right Shift** opens **AetherisMenuScreen** (classic menu).

## Profile System

`config/ProfileManager` saves/loads state and keybinds to `.minecraft/aetheris/profile.json`.
- Auto-saves on state change.
- Loaded on client initialization.

## GUI System

Multiple GUIs available:
- **PauseScreen Quick Buttons** (Pause Menu) — Quick access to ClickGUI, SeedCracker Config, Xray Ores, Alt Manager.
- **AetherisMenuScreen** (Right Ctrl + Right Shift) — Simple category list with scrolling, toggle, keybind assignment, and bottom quick navigation bar.
- **ClickGUI** (Right Shift and Pause Menu) — Aristois/Wurst/Meteor-inspired premium GUI. It uses responsive horizontal category columns (top-most z-index click detection), a compact Keybinds/Xray/Alts/Seed command dock, direct-drawn module rows, per-column scrolling, search, inline keybind/settings drawers, and a fade-in overlay.
- **AltManagerScreen** — Offline account profile switcher for dynamic username changes.
- **XrayBlockSelectorScreen** — Full Minecraft block registry selector with 3-column layout (block icon, name EN/IT, checkbox), search/filter, and buttons for Default/Clear All/Select All.
- **KeybindManagerScreen** — Searchable keybind manager for all modules.
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
| ChatSignature Mixin | `ClientCore/.../mixins/ChatSignatureMixin.java` |
| SeedCracker Config GUI | `ClientCore/.../gui/SeedCrackerConfigScreen.java` |
| Pause Menu Mixin | `ClientCore/.../mixins/PauseScreenMixin.java` |
| Server Audit Tool (proprio server) | `tools/aetheris_server_audit.py` |
| Client entrypoint | `ClientCore/.../AetherisClient.java` |
| Aetheris mixin config | `ClientCore/src/main/resources/aetheris.mixins.json` |
| SeedCracker mixin config | `ClientCore/src/main/resources/seedcracker.mixins.json` |
| Fabric mod manifest | `ClientCore/src/main/resources/fabric.mod.json` |
| Testing Checklist Excel | `Aetheris_Checklist_Test.xlsx` |
| Web Installer backend | `Installer/server.js` |
| Root installer entry | `src/main/java/me/deftware/installer/Main.java` |

## Module Tracking & Guidance for AI Agents

- **Testing Checklist**: Track testing progress and bug notes in `Aetheris_Checklist_Test.xlsx`.
- **Guidance for AI Assistants / LLMs**:
  1. When continuing development or debugging, read `Aetheris_Checklist_Test.xlsx` to understand current module verification status.
  2. Always use **Mojang Official Mappings** for Minecraft 1.21.11 (Java 21). Do not mix Yarn mapping class names.
  3. Keep `AGENTS.md` and `CLAUDE.md` synchronized whenever project structure, architecture, or key guidelines change.
  4. **CRITICAL DEBUGGING NOTE FOR MIXINS**: In Minecraft 1.21.11, if a Mixin fails to apply at runtime (e.g. an `@Accessor` method is not marked `abstract`), Fabric throws a `RuntimeException`. If this happens while connected to a world, the networking thread catches it, forcefully disconnects the player, and triggers `clearClientLevel()`. This causes `updateScreenAndTick()` to tick the world with `mc.player == null`, resulting in cascaded `NullPointerException`s in `GameRenderer.renderLevel` and `MultiPlayerGameMode.ensureHasSentCarriedItem`. **If you see an NPE involving a null player or camera entity, ALWAYS check the start of `latest.log` for a Mixin application failure.**
  5. **Mixin Accessors**: Always ensure that `@Accessor` and `@Invoker` methods in Mixins are strictly `abstract` to prevent the aforementioned crash.

## Module List (67 total)

**Combat (14):** KillAura, Velocity, Criticals, Reach, AutoArmor, AutoTotem, TriggerBot, Surround, AimAssist, SelfTrap, BedAura, BedTrap, CrystalAura (con auto-placement), BowAimbot (con anticipo balistico)
**Movement (12):** AutoSprint, Speed, Fly, NoFall, Step, NoSlowdown, NoClip, BunnyJump, Jetpack, Sneak, AutoWalk, Gesu
**Render (13):** FullBright, ESP, NoHurtCam, Xray, NameTags, Tracers, FreeCam, ItemESP, StorageESP (con hideEmpty), CameraClip, Trajectories, Waypoints (marcatura 3D), SpectatorDetector
**World (15):** FastBreak, Scaffold, Timer, AutoTool, InstalledPlugins, LiquidInteract, AutoSign, AutoFarm, AirPlace, AutoBrewer, AutoSmelter, StrongholdFinder, PacketLogger, ServerFinder, PluginScanner
**Player (12):** AutoRespawn, FastPlace, NoHunger, ChestStealer, AutoFish, InventoryCleaner, AntiAFK, AutoEat, InventorySort, AntiDetect, NoChatReports, PermissionViewer
**SeedCracker (1):** SeedCrackerModule

*Batch 2026-08 (Mappatura Qwen3.8-max): AutoBrewer, AutoSmelter, StrongholdFinder, PacketLogger, ServerFinder, Waypoints, NoChatReports, BowAimbot, AutoWalk.*
*PluginScanner (PluginScanner.java + DiscardedPayloadMixin + hook handleSystemChat in AetherisClientPacketListenerMixin): scan /plugins, tab-probe comandi, brand payload, plugin channels, rileva PEX/LuckPerms/GroupManager, probe comandi permessi. Firma-DB `PLUGIN_CATEGORIES` con report categorizzato (Permessi/Anti-cheat/World-edit/Economia/…) — solo dati pubblici, nessun accesso ai file del server.*
