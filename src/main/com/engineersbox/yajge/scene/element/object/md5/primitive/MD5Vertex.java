package com.engineersbox.yajge.scene.element.object.md5.primitive;

import org.joml.Vector2f;

public class MD5Vertex {

    private int index;
    private Vector2f textCoords;
    private int startWeight;
    private int weightCount;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public Vector2f getTextCoords() {
        return this.textCoords;
    }

    public void setTextCoords(final Vector2f textCoords) {
        this.textCoords = textCoords;
    }

    public int getStartWeight() {
        return this.startWeight;
    }

    public void setStartWeight(final int startWeight) {
        this.startWeight = startWeight;
    }

    public int getWeightCount() {
        return this.weightCount;
    }

    public void setWeightCount(final int weightCount) {
        this.weightCount = weightCount;
    }

    @Override
    public String toString() {
        return "[index: " + this.index + ", textCoods: " + this.textCoords
                + ", startWeight: " + this.startWeight + ", weightCount: " + this.weightCount + "]";
    }
}
