package com.engineersbox.yajge.element.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {
    private final Matrix4f projectionMatrix;
    private final Matrix4f worldMatrix;

    public Transform() {
        worldMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(final float fov,
                                              final float width,
                                              final float height,
                                              final float zNear,
                                              final float zFar) {
        return projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
    }

    public Matrix4f getWorldMatrix(final Vector3f offset,
                                   final Vector3f rotation,
                                   final float scale) {
        return worldMatrix.translation(offset).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(scale);
    }
}
