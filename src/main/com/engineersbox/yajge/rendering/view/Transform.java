package com.engineersbox.yajge.rendering.view;

import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {
    private final Matrix4f projectionMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f orthoMatrix;
    private final Matrix4f orthoModelMatrix;

    public Transform() {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix(final float fov,
                                           final float width,
                                           final float height,
                                           final float zNear,
                                           final float zFar) {
        this.projectionMatrix.identity();
        return this.projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }

    public Matrix4f updateViewMatrix(final Camera camera) {
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
        this.orthoMatrix.identity();
        this.orthoMatrix.setOrtho2D(left, right, bottom, top);
        return this.orthoMatrix;
    }

    public Matrix4f buildModelViewMatrix(final SceneElement sceneElement,
                                         final Matrix4f viewMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        this.modelViewMatrix.set(viewMatrix);
        return this.modelViewMatrix.mul(this.modelMatrix);
    }

    public Matrix4f buildOrthoProjModelMatrix(final SceneElement sceneElement,
                                              final Matrix4f orthoMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        this.orthoModelMatrix.set(orthoMatrix);
        return this.orthoModelMatrix;
    }
}
