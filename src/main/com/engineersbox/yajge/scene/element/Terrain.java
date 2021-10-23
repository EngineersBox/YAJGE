package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.rendering.primitive.HeightMapMesh;

public class Terrain {

    private final SceneElement[] sceneElements;

    public Terrain(final int blocksPerRow,
                   final float scale,
                   final float minY,
                   final float maxY,
                   final String heightMap,
                   final String textureFile,
                   final int textInc) {
        this.sceneElements = new SceneElement[blocksPerRow * blocksPerRow];
        final HeightMapMesh heightMapMesh = new HeightMapMesh(minY, maxY, heightMap, textureFile, textInc);
        for (int row = 0; row < blocksPerRow; row++) {
            for (int col = 0; col < blocksPerRow; col++) {
                final float xDisplacement = (col - ((float) blocksPerRow - 1) / (float) 2) * scale * HeightMapMesh.getXLength();
                final float zDisplacement = (row - ((float) blocksPerRow - 1) / (float) 2) * scale * HeightMapMesh.getZLength();

                final SceneElement terrainBlock = new SceneElement(heightMapMesh.getMesh());
                terrainBlock.setScale(scale);
                terrainBlock.setPosition(xDisplacement, 0, zDisplacement);
                this.sceneElements[row * blocksPerRow + col] = terrainBlock;
            }
        }
    }

    public SceneElement[] getSceneElements() {
        return this.sceneElements;
    }

}
