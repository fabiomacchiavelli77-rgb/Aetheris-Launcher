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
