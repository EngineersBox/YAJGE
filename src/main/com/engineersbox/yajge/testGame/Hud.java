package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.util.AllocUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Hud {

    private static final String FONT_NAME = "BOLD";

    private long vg;
    private NVGColor colour;
    private ByteBuffer fontBuffer;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private DoubleBuffer posX;
    private DoubleBuffer posY;

    private int counter;

    public void init(final Window window) {
        this.vg = nvgCreate((window.getOptions().antialiasing() ? NVG_ANTIALIAS : 0) | NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new RuntimeException("Could not init nanovg");
        }

        this.fontBuffer = ResourceLoader.ioResourceToByteBuffer("assets/game/fonts/OpenSans-Bold.ttf");
        final int font = nvgCreateFontMem(this.vg, FONT_NAME, this.fontBuffer, 0);
        if (font == -1) {
            throw new RuntimeException("Could not add font");
        }
        this.colour = NVGColor.create();
        this.posX = MemoryUtil.memAllocDouble(1);
        this.posY = MemoryUtil.memAllocDouble(1);
        this.counter = 0;
    }

    public void render(final Window window) {
        nvgBeginFrame(this.vg, window.getWidth(), window.getHeight(), 1);

        // Upper ribbon
        nvgBeginPath(this.vg);
        nvgRect(this.vg, 0, window.getHeight() - 100, window.getWidth(), 50);
        nvgFillColor(this.vg, rgba(0x23, 0xa1, 0xf1, 200, this.colour));
        nvgFill(this.vg);

        // Lower ribbon
        nvgBeginPath(this.vg);
        nvgRect(this.vg, 0, window.getHeight() - 50f, window.getWidth(), 10);
        nvgFillColor(this.vg, rgba(0xc1, 0xe3, 0xf9, 200, this.colour));
        nvgFill(this.vg);

        glfwGetCursorPos(window.getWindowHandle(), this.posX, this.posY);
        final int xcenter = 50;
        final int ycenter = window.getHeight() - 75;
        final int radius = 20;
        final int x = (int) this.posX.get(0);
        final int y = (int) this.posY.get(0);
        final boolean hover = Math.pow(x - xcenter, 2) + Math.pow(y - ycenter, 2) < Math.pow(radius, 2);

        // Circle
        nvgBeginPath(this.vg);
        nvgCircle(this.vg, xcenter, ycenter, radius);
        nvgFillColor(this.vg, rgba(0xc1, 0xe3, 0xf9, 200, this.colour));
        nvgFill(this.vg);

        // Clicks Text
        nvgFontSize(this.vg, 25.0f);
        nvgFontFace(this.vg, FONT_NAME);
        nvgTextAlign(this.vg, NVG_ALIGN_CENTER | NVG_ALIGN_TOP);
        if (hover) {
            nvgFillColor(this.vg, rgba(0x00, 0x00, 0x00, 255, this.colour));
        } else {
            nvgFillColor(this.vg, rgba(0x23, 0xa1, 0xf1, 255, this.colour));

        }
        nvgText(this.vg, 50, window.getHeight() - 87, String.format("%02d", this.counter));

        // Render hour text
        nvgFontSize(this.vg, 40.0f);
        nvgFontFace(this.vg, FONT_NAME);
        nvgTextAlign(this.vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFillColor(this.vg, rgba(0xe6, 0xea, 0xed, 255, this.colour));
        nvgText(this.vg, window.getWidth() - 150, window.getHeight() - 95, this.dateFormat.format(new Date()));

        nvgEndFrame(this.vg);
        window.restoreState();
    }

    public void incCounter() {
        this.counter++;
        if (this.counter > 99) {
            this.counter = 0;
        }
    }

    private NVGColor rgba(final int r,
                          final int g,
                          final int b,
                          final int a,
                          final NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public void cleanup() {
        nvgDelete(this.vg);
        AllocUtils.freeAll(this.posX, this.posY);
    }
}
