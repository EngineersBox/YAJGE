package com.engineersbox.yajge.animation;

import org.joml.Matrix4f;

import java.util.Arrays;

public class AnimatedFrame {

    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    public static final int MAX_JOINTS = 150;

    private final Matrix4f[] jointMatrices;

    public AnimatedFrame() {
        this.jointMatrices = new Matrix4f[MAX_JOINTS];
        Arrays.fill(this.jointMatrices, IDENTITY_MATRIX);
    }

    public Matrix4f[] getJointMatrices() {
        return this.jointMatrices;
    }

    public void setMatrix(final int pos, final Matrix4f jointMatrix) {
        this.jointMatrices[pos] = jointMatrix;
    }
}
