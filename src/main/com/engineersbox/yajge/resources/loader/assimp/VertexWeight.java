package com.engineersbox.yajge.resources.loader.assimp;

public class VertexWeight {
    private final int boneId;
    private int vertexId;
    private float weight;

    public VertexWeight(final int boneId,
                        final int vertexId,
                        final float weight) {
        this.boneId = boneId;
        this.vertexId = vertexId;
        this.weight = weight;
    }

    public int getBoneId() {
        return this.boneId;
    }

    public int getVertexId() {
        return this.vertexId;
    }

    public float getWeight() {
        return this.weight;
    }

    public void setVertexId(final int vertexId) {
        this.vertexId = vertexId;
    }

    public void setWeight(final float weight) {
        this.weight = weight;
    }
}
