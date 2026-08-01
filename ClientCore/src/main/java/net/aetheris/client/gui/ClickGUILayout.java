package net.aetheris.client.gui;

public final class ClickGUILayout {
    public static final int OUTER_MARGIN = 8;
    public static final int COLUMN_GAP = 4;
    public static final int MAX_COLUMN_WIDTH = 118;
    public static final int MIN_COLUMN_WIDTH = 60;
    public static final int COLUMN_TOP = 38;
    public static final int HEADER_HEIGHT = 22;
    public static final int ROW_HEIGHT = 16;
    public static final int FOOTER_RESERVE = 62;

    private ClickGUILayout() { }

    public static Layout calculate(int viewportWidth, int viewportHeight, int categoryCount) {
        int count = Math.max(1, categoryCount);
        int availableWidth = Math.max(count,
            viewportWidth - OUTER_MARGIN * 2 - COLUMN_GAP * (count - 1));
        int columnWidth = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, availableWidth / count));
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
