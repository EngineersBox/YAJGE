package com.engineersbox.yajge.resources.loader.assimp;

import org.joml.Matrix4f;

public class Bone {
    private final int boneId;
    private final String boneName;
    private final Matrix4f offsetMatrix;

    public Bone(final int boneId, final String boneName, final Matrix4f offsetMatrix) {
        this.boneId = boneId;
        this.boneName = boneName;
        this.offsetMatrix = offsetMatrix;
    }

    public int getBoneId() {
        return this.boneId;
    }

    public String getBoneName() {
        return this.boneName;
    }

    public Matrix4f getOffsetMatrix() {
        return this.offsetMatrix;
    }
}
