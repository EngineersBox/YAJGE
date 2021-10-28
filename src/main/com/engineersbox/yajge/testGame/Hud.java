package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.assets.font.FontTexture;
import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.rendering.object.composite.Mesh;
import com.engineersbox.yajge.resources.loader.OBJLoader;
import com.engineersbox.yajge.scene.gui.IHud;
import com.engineersbox.yajge.scene.gui.TextElement;
import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Vector4f;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Hud implements IHud {
    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private final SceneElement[] sceneElements;
    private final TextElement statusTextElement;
    private final SceneElement compassItem;

    public Hud(final String statusText) {
        final FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextElement = new TextElement(statusText, fontTexture);
        this.statusTextElement.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 1, 1, 1));
        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/compass.obj");
        final Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        this.compassItem = new SceneElement(mesh);
        this.compassItem.setScale(40.0f);
        this.compassItem.setRotation(0f, 0f, 180f);
        this.sceneElements = new SceneElement[]{ this.statusTextElement, this.compassItem };
    }

    public void setStatusText(final String statusText) {
        this.statusTextElement.setText(statusText);
    }

    public void rotateCompass(final float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }

    @Override
    public SceneElement[] getSceneElements() {
        return this.sceneElements;
    }

    public void updateSize(final Window window) {
        this.statusTextElement.setPosition(10f, window.getHeight() - 50f, 0);
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }
}
