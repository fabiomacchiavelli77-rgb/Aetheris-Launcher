# Canonical Aetheris ClickGUI Design

**Status:** Approved by the user on 2026-07-23.

## Goal

Replace the obsolete large-button `AetherisMenuScreen` with a single polished Aetheris control surface. Left Shift, Right Shift, and the Pause screen's Aetheris entry must all open the same `ClickGUI`.

## Scope

- Delete `AetherisMenuScreen.java`; no compatibility wrapper remains.
- Make `ClickGUI` the only module-management screen.
- Preserve existing module toggle, keybind recording, search, per-column scrolling, Xray settings, and SeedCracker settings.
- Preserve quick access to Keybinds, Xray Ores, Alt Manager, and SeedCracker through a compact control dock.
- Do not change module behavior, profile serialization, or non-GUI mixin behavior.

## Interaction and Visual Design

`ClickGUI` remains a direct-drawn Minecraft screen rather than a widget-button screen. Its visual direction is a dark translucent technical console: six category columns, each with its own accent line and enabled-state highlight, over the game world.

- A restrained header identifies Aetheris and reports active-module state without competing with the categories.
- Columns are calculated from the current screen width, centered, and use a bounded panel width and gap instead of a permanently fixed width. For six categories, the panel width is `min(118, max(1, floor((screenWidth - 24 - 30) / 6)))`: 12-pixel outer margins and five 6-pixel gaps are reserved before the six panels are sized. The result retains six aligned columns without horizontal clipping and avoids the edge-to-edge, oversized button presentation of the removed screen.
- A bottom command dock exposes `Keybinds`, `Xray`, `Alts`, and `Seed` as compact controls; the search field stays centered and visually separate.
- A left click toggles a module; a right click opens its inline drawer. The drawer keeps key assignment and context-specific configuration access.
- Escape, Left Shift, and Right Shift close the screen. This makes either launcher key act as a predictable toggle.
- Search changes reset or clamp per-column scroll offsets, so filtered results never create an empty-looking column. Expanded drawers are drawn only for visible matching rows.

## Architecture

### Canonical entry points

`KeyboardMixin` imports and constructs only `ClickGUI` for either Shift key. `PauseScreenMixin` constructs `ClickGUI` for its Aetheris button. All imports and references to `AetherisMenuScreen` are removed before deleting that class.

### Layout boundary

Responsive geometry is calculated in a small GUI-local layout helper with no Minecraft runtime dependency. It provides column width, horizontal gap, column start position, top offset, and command-dock bounds from viewport dimensions. `ClickGUI` consumes this geometry for rendering, hit-testing, and scrolling, so the three paths cannot drift apart.

### State and persistence

`ClickGUI` remains responsible for transient GUI state: search query and focus, binding target, elapsed open animation, expanded module per category, and per-column scroll offset. Module toggles and assigned keybinds continue to call `ProfileManager.getInstance().onModuleChanged()`.

## Error Handling and Edge Cases

- Layout math must keep widths, row capacity, and scroll upper bounds non-negative on small viewports.
- Search and resize must clamp each column's scroll offset against its currently filtered module list.
- A drawer that belongs to a filtered-out or off-screen module must not reserve or accept space.
- The command dock must return immediately after opening a destination screen so a click cannot also toggle a module.
- The module keyboard hook continues to ignore keybind processing while any screen is open.

## Validation

- Add focused unit tests for the pure layout helper: normal six-column centering, narrow-viewport bounds, and non-negative row capacity/scroll bounds.
- Run the added test class through Gradle after observing it fail before the implementation.
- Run `ClientCore\gradlew.bat build` to compile all Minecraft/Fabric sources and tests.
- Run `ClientCore\gradlew.bat runClient` for a manual check that both Shift keys and the Pause screen open the same GUI, module controls work, and the utility dock routes correctly.

## Acceptance Criteria

1. `AetherisMenuScreen.java` and all Java references to it are gone.
2. Left Shift, Right Shift, and the Pause menu's Aetheris button all open `ClickGUI`.
3. The ClickGUI has polished, responsive category columns plus a compact utility dock; it contains no large module buttons.
4. Existing module toggle, keybind, search, scrolling, Xray, and SeedCracker behavior remain available.
5. The ClientCore Gradle build completes successfully.
