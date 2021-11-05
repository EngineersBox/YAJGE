package com.engineersbox.yajge.rendering.view;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Vector3f rotation;
    private Matrix4f viewMatrix;

    public Camera() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.viewMatrix = new Matrix4f();
    }

    public Camera(final Vector3f position,
                  final Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(final float x,
                            final float y,
                            final float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }

    public Matrix4f updateViewMatrix() {
        return Transform.updateGenericViewMatrix(
                this.position,
                this.rotation,
                this.viewMatrix
        );
    }

    public void movePosition(final float offsetX,
                             final float offsetY,
                             final float offsetZ) {
        if (offsetZ != 0) {
            this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y)) * -1.0f * offsetZ;
            this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y - 90)) * -1.0f * offsetX;
            this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y - 90)) * offsetX;
        }
        this.position.y += offsetY;
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public void setRotation(final float x,
                            final float y,
                            final float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public void moveRotation(final float offsetX,
                             final float offsetY,
                             final float offsetZ) {
        this.rotation.x += offsetX;
        this.rotation.y += offsetY;
        this.rotation.z += offsetZ;
    }
}