package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.resources.assets.font.FontTexture;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.gui.IHud;
import com.engineersbox.yajge.scene.gui.TextElement;
import org.joml.Vector4f;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Hud implements IHud {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private final SceneElement[] sceneElements;

    private final TextElement statusFontTexture;

    public Hud(final String statusText) {
        final FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusFontTexture = new TextElement(statusText, fontTexture);
        this.statusFontTexture.getMesh().getMaterial().setAmbientColour(new Vector4f(0.5f, 0.5f, 0.5f, 10f));

        // Create list that holds the items that compose the HUD
        this.sceneElements = new SceneElement[]{this.statusFontTexture};
    }

    public void setStatusText(final String statusText) {
        this.statusFontTexture.setText(statusText);
    }
    
    @Override
    public SceneElement[] getGameItems() {
        return this.sceneElements;
    }
   
    public void updateSize(final Window window) {
        this.statusFontTexture.setPosition(10f, window.getHeight() - 50f, 0);
    }
}
