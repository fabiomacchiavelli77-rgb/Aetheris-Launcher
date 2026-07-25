# Canonical ClickGUI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `ClickGUI` the sole responsive Aetheris module menu, opened by either Shift key and the Pause-screen shortcut.

**Architecture:** Put viewport calculations in a pure `ClickGUILayout` helper, with unit tests that drive the geometry before any Minecraft-dependent rendering is changed. `ClickGUI` consumes the helper for panel positions, scroll limits, search, and the command dock. Remove the former `AetherisMenuScreen` only after every entry point has moved to `ClickGUI`.

**Tech Stack:** Java 21, Fabric Loom 1.9, Minecraft 1.21.4 official mappings, JUnit Jupiter 5.10.2, Gradle 9.6.

## Global Constraints

- Keep Mojang official mappings and Java 21; do not add Fabric or Minecraft dependencies.
- Use direct `GuiGraphics` rendering for the ClickGUI; do not reintroduce Minecraft `Button` widgets for module rows.
- Keep module toggle and keybind persistence through `ProfileManager`.
- Preserve the existing Xray and SeedCracker configuration routes, and add compact routes for Keybinds and Alt Manager.
- Keep `AGENTS.md` and `CLAUDE.md` byte-for-byte synchronized after their GUI documentation is updated.
- Validate with `ClientCore\gradlew.bat test` and `ClientCore\gradlew.bat build` before reporting completion.

## File Structure

| File | Responsibility |
| --- | --- |
| `ClientCore/build.gradle` | Provides JUnit Jupiter and enables the Gradle JUnit Platform runner. |
| `ClientCore/src/test/java/net/aetheris/client/gui/ClickGUILayoutTest.java` | Locks responsive panel sizing and scroll-bound behavior before rendering changes. |
| `ClientCore/src/main/java/net/aetheris/client/gui/ClickGUILayout.java` | Pure, Minecraft-independent geometry and scroll-bound calculator. |
| `ClientCore/src/main/java/net/aetheris/client/gui/ClickGUI.java` | Canonical rendered module menu, interaction logic, search, and utility dock. |
| `ClientCore/src/main/java/net/aetheris/client/mixins/KeyboardMixin.java` | Routes both Shift keys to ClickGUI while retaining module keybind handling. |
| `ClientCore/src/main/java/net/aetheris/client/mixins/PauseScreenMixin.java` | Routes the Pause-menu Aetheris shortcut to ClickGUI. |
| `ClientCore/src/main/java/net/aetheris/client/gui/AetherisMenuScreen.java` | Removed legacy large-button UI. |
| `AGENTS.md` and `CLAUDE.md` | Synchronized architecture documentation for the unified entry point. |

---

### Task 1: Configure a repeatable GUI-layout test harness

**Files:**
- Modify: `C:\Progetti AI\Aetheris\ClientCore\build.gradle:13-46, 67-71`

**Interfaces:**
- Produces: JUnit Platform test execution for `ClientCore/src/test/java`.
- Consumes: Gradle's built-in `test` task.

- [ ] **Step 1: Confirm the existing baseline test task has no source failures**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test`

Expected: exit code 0; `compileTestJava NO-SOURCE` and `test NO-SOURCE`. The current baseline was observed with this result on 2026-07-23.

- [ ] **Step 2: Add JUnit Jupiter and enable the JUnit Platform**

Add this exact dependency inside `dependencies { ... }` and this task configuration after the `java { ... }` block:

~~~groovy
testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
~~~

~~~groovy
tasks.named('test') {
    useJUnitPlatform()
}
~~~

- [ ] **Step 3: Verify the empty JUnit-enabled test task still succeeds**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test`

Expected: exit code 0 and no Java compilation errors.

- [ ] **Step 4: Commit the harness change**

~~~powershell
git add ClientCore/build.gradle
git commit -m "test(gui): enable JUnit layout tests"
~~~

### Task 2: Specify responsive layout behavior with failing tests

**Files:**
- Create: `C:\Progetti AI\Aetheris\ClientCore\src\test\java\net\aetheris\client\gui\ClickGUILayoutTest.java`

**Interfaces:**
- Consumes: `ClickGUILayout.calculate(int viewportWidth, int viewportHeight, int categoryCount)`.
- Consumes: `ClickGUILayout.Layout.columnWidth()`, `columnsStartX()`, `columnX(int)`, `maxVisibleRows()`, `maxScrollOffset(int)`, and `clampScrollOffset(int, int)`.
- Produces: executable requirements for centered full-width-safe columns and bounded scrolling.

- [ ] **Step 1: Write the failing layout tests**

~~~java
package net.aetheris.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClickGUILayoutTest {
    @Test
    void centersSixMaximumWidthColumns() {
        ClickGUILayout.Layout layout = ClickGUILayout.calculate(1280, 720, 6);

        assertEquals(118, layout.columnWidth());
        assertEquals(271, layout.columnsStartX());
        assertEquals(271, layout.columnX(0));
        assertEquals(271 + 5 * (118 + 6), layout.columnX(5));
    }

    @Test
    void keepsEveryColumnInsideANarrowViewport() {
        ClickGUILayout.Layout layout = ClickGUILayout.calculate(320, 240, 6);

        assertTrue(layout.columnWidth() >= 1);
        assertTrue(layout.columnsStartX() >= 0);
        assertTrue(layout.columnX(5) + layout.columnWidth() <= 320);
    }

    @Test
    void keepsRowCapacityAndScrollBoundsNonNegative() {
        ClickGUILayout.Layout layout = ClickGUILayout.calculate(320, 70, 6);

        assertTrue(layout.maxVisibleRows() >= 1);
        assertEquals(0, layout.maxScrollOffset(0));
        assertEquals(0, layout.clampScrollOffset(-4, 0));
        assertEquals(3, layout.clampScrollOffset(99, layout.maxVisibleRows() + 3));
    }
}
~~~

- [ ] **Step 2: Run the test to verify it fails because the helper is absent**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test --tests net.aetheris.client.gui.ClickGUILayoutTest`

Expected: `compileTestJava FAILED` with `cannot find symbol` for `ClickGUILayout`, not a JUnit discovery failure.

### Task 3: Implement the pure layout helper and make its tests green

**Files:**
- Create: `C:\Progetti AI\Aetheris\ClientCore\src\main\java\net\aetheris\client\gui\ClickGUILayout.java`
- Test: `C:\Progetti AI\Aetheris\ClientCore\src\test\java\net\aetheris\client\gui\ClickGUILayoutTest.java`

**Interfaces:**
- Produces: `ClickGUILayout.calculate(int, int, int)` and nested immutable `Layout` for all ClickGUI geometry and scroll-bound calculations.
- Consumes: primitive viewport dimensions only; it must import no Minecraft classes.

- [ ] **Step 1: Implement the smallest layout helper that satisfies the tests**

~~~java
package net.aetheris.client.gui;

public final class ClickGUILayout {
    public static final int OUTER_MARGIN = 12;
    public static final int COLUMN_GAP = 6;
    public static final int MAX_COLUMN_WIDTH = 118;
    public static final int COLUMN_TOP = 38;
    public static final int HEADER_HEIGHT = 22;
    public static final int ROW_HEIGHT = 16;
    public static final int FOOTER_RESERVE = 62;

    private ClickGUILayout() { }

    public static Layout calculate(int viewportWidth, int viewportHeight, int categoryCount) {
        int count = Math.max(1, categoryCount);
        int availableWidth = Math.max(count,
            viewportWidth - OUTER_MARGIN * 2 - COLUMN_GAP * (count - 1));
        int columnWidth = Math.max(1, Math.min(MAX_COLUMN_WIDTH, availableWidth / count));
        int totalWidth = columnWidth * count + COLUMN_GAP * (count - 1);
        int startX = Math.max(0, (viewportWidth - totalWidth) / 2);
        int topY = Math.max(16, Math.min(COLUMN_TOP, viewportHeight / 6));
        int maxVisibleRows = Math.max(1,
            (viewportHeight - topY - HEADER_HEIGHT - FOOTER_RESERVE) / ROW_HEIGHT);
        return new Layout(columnWidth, startX, topY, maxVisibleRows);
    }

    public record Layout(int columnWidth, int columnsStartX, int columnTopY, int maxVisibleRows) {
        public int columnX(int index) {
            return columnsStartX + Math.max(0, index) * (columnWidth + COLUMN_GAP);
        }

        public int maxScrollOffset(int moduleCount) {
            return Math.max(0, moduleCount - maxVisibleRows);
        }

        public int clampScrollOffset(int requestedOffset, int moduleCount) {
            return Math.max(0, Math.min(requestedOffset, maxScrollOffset(moduleCount)));
        }
    }
}
~~~

- [ ] **Step 2: Run the focused tests and confirm green**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test --tests net.aetheris.client.gui.ClickGUILayoutTest`

Expected: exit code 0 and `3 tests completed, 0 failed`.

- [ ] **Step 3: Commit the tested layout boundary**

~~~powershell
git add ClientCore/src/main/java/net/aetheris/client/gui/ClickGUILayout.java ClientCore/src/test/java/net/aetheris/client/gui/ClickGUILayoutTest.java
git commit -m "feat(gui): add responsive layout helper"
~~~

### Task 4: Refine ClickGUI into the responsive canonical control surface

**Files:**
- Modify: `C:\Progetti AI\Aetheris\ClientCore\src\main\java\net\aetheris\client\gui\ClickGUI.java:22-487`
- Test: `C:\Progetti AI\Aetheris\ClientCore\src\test\java\net\aetheris\client\gui\ClickGUILayoutTest.java`

**Interfaces:**
- Consumes: `ClickGUILayout.Layout` from Task 3.
- Produces: a six-panel responsive UI with a compact `Keybinds` / `Xray` / `Alts` / `Seed` dock.
- Preserves: `ClickGUI()` no-argument constructor, module click actions, and `Screen` parent routing.

- [ ] **Step 1: Replace fixed layout calculations with the tested helper**

Use `ClickGUILayout.HEADER_HEIGHT` and `ClickGUILayout.ROW_HEIGHT` in place of local dimensions. In `init`, assign each column from the helper and clamp its scroll position:

~~~java
private ClickGUILayout.Layout layout() {
    return ClickGUILayout.calculate(width, height, columns.size());
}

private void applyLayoutAndClamp() {
    ClickGUILayout.Layout layout = layout();
    for (int index = 0; index < columns.size(); index++) {
        Column column = columns.get(index);
        column.x = layout.columnX(index);
        column.y = layout.columnTopY();
        List<Module> modules = column.filteredModules(searchQuery);
        column.scrollOffset = layout.clampScrollOffset(column.scrollOffset, modules.size());
        if (column.expandedModule != null && !modules.contains(column.expandedModule)) {
            column.expandedModule = null;
        }
    }
}
~~~

Call `applyLayoutAndClamp()` from `init`, immediately after changing `searchQuery`, and before rendering columns. Use `layout().maxVisibleRows()` for rendering, hit-testing, and mouse wheel bounds. Calculate `totalH` with an expanded drawer only when `expandedModule` is between `startIdx` and `endIdx`.

- [ ] **Step 2: Add the Aetheris header and the compact command dock**

Add this enum and route handler to `ClickGUI`; render the controls as direct filled rectangles and labels, using the existing hover treatment.

~~~java
private enum UtilityAction {
    KEYBINDS("Keybinds"), XRAY("Xray"), ALTS("Alts"), SEED("Seed");

    private final String label;
    UtilityAction(String label) { this.label = label; }
}

private void openUtility(UtilityAction action) {
    switch (action) {
        case KEYBINDS -> Minecraft.getInstance().setScreen(new KeybindManagerScreen(this));
        case XRAY -> Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
        case ALTS -> Minecraft.getInstance().setScreen(new AltManagerScreen(this));
        case SEED -> Minecraft.getInstance().setScreen(new SeedCrackerConfigScreen(this));
    }
}
~~~

The header must show `AETHERIS` and the count of enabled modules. Place the dock above the centered search field, use four equal compact regions with a 4-pixel gap, and return `true` immediately after `openUtility(...)` in `mouseClicked`.

- [ ] **Step 3: Make keyboard and search behavior match the unified launcher**

Use this close condition and reset or clamp columns whenever the query changes:

~~~java
if (key == GLFW.GLFW_KEY_ESCAPE
        || key == GLFW.GLFW_KEY_LEFT_SHIFT
        || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
    onClose();
    return true;
}
~~~

~~~java
private void setSearchQuery(String query) {
    searchQuery = query;
    applyLayoutAndClamp();
}
~~~

Route both backspace and `charTyped` through `setSearchQuery`. Do not call `ProfileManager` after `mod.toggle()`, because `Module.setEnabled()` already persists the state; keep the explicit profile save after `setKeybind(...)` because `setKeybind` itself has no persistence hook.

- [ ] **Step 4: Run layout tests and compile the GUI**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test --tests net.aetheris.client.gui.ClickGUILayoutTest; .\gradlew.bat compileJava`

Expected: exit code 0, three layout tests pass, and all Minecraft GUI classes compile.

- [ ] **Step 5: Commit the responsive GUI redesign**

~~~powershell
git add ClientCore/src/main/java/net/aetheris/client/gui/ClickGUI.java
git commit -m "feat(gui): refine canonical ClickGUI"
~~~

### Task 5: Route every former menu entry point to ClickGUI and remove the legacy class

**Files:**
- Modify: `C:\Progetti AI\Aetheris\ClientCore\src\main\java\net\aetheris\client\mixins\KeyboardMixin.java:3-35`
- Modify: `C:\Progetti AI\Aetheris\ClientCore\src\main\java\net\aetheris\client\mixins\PauseScreenMixin.java:3-35`
- Delete: `C:\Progetti AI\Aetheris\ClientCore\src\main\java\net\aetheris\client\gui\AetherisMenuScreen.java`

**Interfaces:**
- Produces: one `ClickGUI` entry path for either Shift key and the Pause-screen Aetheris button.
- Preserves: module keybind handling when `Minecraft.screen == null`.

- [ ] **Step 1: Collapse the Shift branches in KeyboardMixin**

Remove the `AetherisMenuScreen` import. Replace the two menu branches with:

~~~java
// Either Shift toggles the canonical Aetheris control surface.
if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
    mc.setScreen(new ClickGUI());
    return;
}
~~~

- [ ] **Step 2: Replace the Pause-menu destination**

Replace the old import with `import net.aetheris.client.gui.ClickGUI;` and use:

~~~java
Minecraft.getInstance().setScreen(new ClickGUI());
~~~

for the `Aetheris Menu` button. Do not change the three other Pause-menu buttons.

- [ ] **Step 3: Delete the legacy implementation only after its callers are moved**

Run: `git rm ClientCore/src/main/java/net/aetheris/client/gui/AetherisMenuScreen.java`

- [ ] **Step 4: Verify that no Java reference survives**

Run: `rg -n --glob '*.java' 'AetherisMenuScreen' ClientCore/src/main/java`

Expected: no matches; `rg` exits 1 because the searched text is absent.

- [ ] **Step 5: Compile and commit the entry-point migration**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat compileJava`

Expected: exit code 0.

~~~powershell
git add ClientCore/src/main/java/net/aetheris/client/mixins/KeyboardMixin.java ClientCore/src/main/java/net/aetheris/client/mixins/PauseScreenMixin.java
git commit -m "refactor(gui): remove legacy Aetheris menu"
~~~

### Task 6: Synchronize project documentation and verify the full deliverable

**Files:**
- Modify: `C:\Progetti AI\Aetheris\AGENTS.md:41-43, 75-82, 93-101`
- Modify: `C:\Progetti AI\Aetheris\CLAUDE.md:41-43, 75-82, 93-101`

**Interfaces:**
- Produces: synchronized agent instructions that describe one canonical ClickGUI.
- Consumes: final entry-point behavior from Task 5.

- [ ] **Step 1: Update both architecture documents with identical GUI wording**

Make these synchronized substitutions in both files:

~~~markdown
- `KeyboardMixin` — either Shift toggles `ClickGUI`; module keybinds work only while no screen is open.
~~~

~~~markdown
- Left Shift, Right Shift, and the Pause Menu's **Aetheris Menu** button open the same **ClickGUI**.
~~~

~~~markdown
- **ClickGUI** (Left/Right Shift and Pause Menu) — Aristois/Wurst/Meteor-inspired premium GUI. It uses responsive horizontal category columns, a compact Keybinds/Xray/Alts/Seed command dock, direct-drawn module rows, per-column scrolling, search, inline keybind/settings drawers, and a fade-in overlay.
~~~

- [ ] **Step 2: Confirm the mirrored files remain identical**

Run: `git diff --no-index -- AGENTS.md CLAUDE.md`

Expected: no output and exit code 0.

- [ ] **Step 3: Run all automated verification**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat test; .\gradlew.bat build`

Expected: both commands exit 0. The test output must report the three `ClickGUILayoutTest` cases with zero failures; the build output must report `BUILD SUCCESSFUL`.

- [ ] **Step 4: Perform the in-client acceptance check**

Run: `cd C:\Progetti AI\Aetheris\ClientCore; .\gradlew.bat runClient`

In the Fabric development client, verify all of these visible behaviors before closing it:

1. Left Shift opens the six-column ClickGUI; Left Shift, Right Shift, and Escape each close it.
2. Right Shift opens the same ClickGUI, not a button-based screen.
3. The Pause menu's `Aetheris Menu` button opens the same ClickGUI.
4. A module left click toggles it; a right click shows its drawer; assigning a key persists after closing and reopening.
5. Search filters categories without blank scrolled columns; mouse wheel scrolls only the hovered category.
6. Keybinds, Xray, Alts, and Seed dock entries open their expected screens and each Back control returns to ClickGUI.

- [ ] **Step 5: Commit the documentation and report evidence**

~~~powershell
git add AGENTS.md CLAUDE.md
git commit -m "docs(gui): document unified ClickGUI"
git status --short
~~~

Expected: `git status --short` prints no lines after the commit.
