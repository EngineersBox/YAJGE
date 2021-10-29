package com.engineersbox.yajge.scene.element.object.md5.primitive;

import org.joml.Vector3f;

public class MD5Weight {

    private int index;
    private int jointIndex;
    private float bias;
    private Vector3f position;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getJointIndex() {
        return this.jointIndex;
    }

    public void setJointIndex(final int jointIndex) {
        this.jointIndex = jointIndex;
    }

    public float getBias() {
        return this.bias;
    }

    public void setBias(final float bias) {
        this.bias = bias;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "[index: " + this.index + ", jointIndex: " + this.jointIndex
                + ", bias: " + this.bias + ", position: " + this.position + "]";
    }
}
