package com.engineersbox.yajge.scene.gui;

import java.util.ArrayList;
import java.util.List;

import com.engineersbox.yajge.rendering.assets.font.CharInfo;
import com.engineersbox.yajge.rendering.assets.font.FontTexture;
import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.scene.element.SceneElement;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.primitive.FloatLists;

public class TextElement extends SceneElement {

    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;

    private final FontTexture fontTexture;
    private String text;

    public TextElement(String text, FontTexture fontTexture) {
        super();
        this.text = text;
        this.fontTexture = fontTexture;
        setMesh(buildMesh());
    }

    private Mesh buildMesh() {
        final List<Float> positions = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final float[] normals = new float[0];
        final List<Integer> indices = new ArrayList<>();
        final char[] characters = text.toCharArray();
        final int numChars = characters.length;

        float startx = 0;
        for(int i=0; i<numChars; i++) {
            final CharInfo charInfo = fontTexture.getCharInfo(characters[i]);

            // Left Top vertex
            positions.add(startx);
            positions.add(0.0f);
            positions.add(ZPOS);
            texCoords.add( (float) charInfo.startX() / (float) fontTexture.getWidth());
            texCoords.add(0.0f);
            indices.add(i * VERTICES_PER_QUAD);

            // Left Bottom vertex
            positions.add(startx);
            positions.add((float) fontTexture.getHeight());
            positions.add(ZPOS);
            texCoords.add((float) charInfo.startX() / (float) fontTexture.getWidth());
            texCoords.add(1.0f);
            indices.add(i * VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add(startx + charInfo.width());
            positions.add((float) fontTexture.getHeight());
            positions.add(ZPOS);
            texCoords.add((float) (charInfo.startX() + charInfo.width()) / fontTexture.getWidth());
            texCoords.add(1.0f);
            indices.add(i * VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add(startx + charInfo.width());
            positions.add(0.0f);
            positions.add(ZPOS);
            texCoords.add((float) (charInfo.startX() + charInfo.width()) / fontTexture.getWidth());
            texCoords.add(0.0f);
            indices.add(i * VERTICES_PER_QUAD + 3);

            // Add indices por left top and bottom right vertices
            indices.add(i * VERTICES_PER_QUAD);
            indices.add(i * VERTICES_PER_QUAD + 2);

            startx += charInfo.width();
        }

        final float[] posArr = positions.stream()
                .collect(Collectors2.collectFloat(
                        Float::floatValue,
                        FloatLists.mutable::empty
                )).toArray();
        final float[] texCoordsArr = texCoords.stream()
                .collect(Collectors2.collectFloat(
                        Float::floatValue,
                        FloatLists.mutable::empty
                )).toArray();
        final int[] indicesArr = indices.stream().mapToInt(i->i).toArray();
        final Mesh mesh = new Mesh(posArr, texCoordsArr, normals, indicesArr);
        mesh.setMaterial(new Material(fontTexture.getTexture()));
        return mesh;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh());
    }
}