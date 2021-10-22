package com.engineersbox.yajge.rendering.view;

import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewModelMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f orthoMatrix;

    public Transform() {
        this.viewModelMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.orthoMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(final float fov,
                                              final float width,
                                              final float height,
                                              final float zNear,
                                              final float zFar) {
        return this.projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
    }

    public Matrix4f getViewModelMatrix(final SceneElement sceneElement,
                                       final Matrix4f viewMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.viewModelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateY((float) Math.toRadians(-rotation.y))
                .rotateZ((float) Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        return new Matrix4f(viewMatrix).mul(this.viewModelMatrix);
    }

    public Matrix4f getViewMatrix(final Camera camera) {
        final Vector3f cameraPos = camera.getPosition();
        final Vector3f rotation = camera.getRotation();
        this.viewMatrix.identity();
        this.viewMatrix.rotate(
                (float) Math.toRadians(rotation.x),
                new Vector3f(1, 0, 0)
        ).rotate(
                (float) Math.toRadians(rotation.y),
                new Vector3f(0, 1, 0)
        );
        this.viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return this.viewMatrix;
    }

    public final Matrix4f getOrthoProjectionMatrix(final float left,
                                                   final float right,
                                                   final float bottom,
                                                   final float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    public Matrix4f getOrthoProjModelMatrix(final SceneElement sceneElement,
                                            final Matrix4f orthoMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        final Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        final Matrix4f orthoMatrixCurr = new Matrix4f(orthoMatrix);
        orthoMatrixCurr.mul(modelMatrix);
        return orthoMatrixCurr;
    }
}
