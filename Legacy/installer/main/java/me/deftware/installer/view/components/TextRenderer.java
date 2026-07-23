package me.deftware.installer.view.components;

import me.deftware.installer.Utils;
import me.deftware.installer.view.Window;
import org.lwjgl.nanovg.NVGColor;

import java.awt.*;
import java.nio.FloatBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextRenderer {

    public static final NVGColor textColor = Utils.getColor(Color.white);

    public static final int small = 18;
    public static final int fontSize = 28;

    public static final int heading = 40;
    public static final int subHeading = 23;

    public static void drawText(String text, int x, int y, int width, int height, boolean centered) {
        drawText(text, x, y, width, height, centered, fontSize);
    }

    public static void drawText(String text, int x, int y, int width, int height, boolean centered, int fontSize) {
        long vg = Window.getInstance().vg;

        nvgFillColor(vg, textColor);
        nvgTextAlign(vg, NVG_ALIGN_MIDDLE);
        nvgFontFace(vg, Utils.defaultFont);
        nvgFontSize(vg, fontSize);

        float textWidth = nvgTextBounds(vg, 0,0, text, (FloatBuffer) null);
        nvgText(vg, x + (centered ? width / 2f - textWidth / 2f : 5), y + height / 2f, text);
    }

}
