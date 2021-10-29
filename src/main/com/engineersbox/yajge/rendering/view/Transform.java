package com.engineersbox.yajge.rendering.view;

import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {

    private static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
    private static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);

    private final Matrix4f projectionMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f viewModelMatrix;
    private final Matrix4f modelLightMatrix;
    private final Matrix4f modelLightViewMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f lightViewMatrix;
    private final Matrix4f orthoProjMatrix;
    private final Matrix4f ortho2DMatrix;
    private final Matrix4f orthoModelMatrix;

    public Transform() {
        this.projectionMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
        this.viewModelMatrix = new Matrix4f();
        this.modelLightMatrix = new Matrix4f();
        this.modelLightViewMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.orthoProjMatrix = new Matrix4f();
        this.ortho2DMatrix = new Matrix4f();
        this.orthoModelMatrix = new Matrix4f();
        this.lightViewMatrix = new Matrix4f();
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
        return this.projectionMatrix.setPerspective(
                fov,
                width / height,
                zNear,
                zFar
        );
    }

    public final Matrix4f getOrthoProjectionMatrix() {
        return this.orthoProjMatrix;
    }

    public Matrix4f updateOrthoProjectionMatrix(final float left,
                                                final float right,
                                                final float bottom,
                                                final float top,
                                                final float zNear,
                                                final float zFar) {
        this.orthoProjMatrix.identity();
        this.orthoProjMatrix.setOrtho(
                left,
                right,
                bottom,
                top,
                zNear,
                zFar
        );
        return this.orthoProjMatrix;
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }
    
    public Matrix4f updateViewMatrix(final Camera camera) {
        return updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), this.viewMatrix);
    }

    public Matrix4f getLightViewMatrix() {
        return this.lightViewMatrix;
    }

    public void setLightViewMatrix(final Matrix4f lightViewMatrix) {
        this.lightViewMatrix.set(lightViewMatrix);
    }

    public Matrix4f updateLightViewMatrix(final Vector3f position, final Vector3f rotation) {
        return updateGenericViewMatrix(
                position,
                rotation,
                this.lightViewMatrix
        );
    }

    private Matrix4f updateGenericViewMatrix(final Vector3f position, final Vector3f rotation, final Matrix4f matrix) {
        matrix.identity();
        matrix.rotate(
                (float) Math.toRadians(rotation.x),
                X_AXIS
        ).rotate(
                (float) Math.toRadians(rotation.y),
                Y_AXIS
        );
        matrix.translate(
                -position.x,
                -position.y,
                -position.z
        );
        return matrix;
    }

    public final Matrix4f getOrtho2DProjectionMatrix(final float left,
                                                     final float right,
                                                     final float bottom,
                                                     final float top) {
        this.ortho2DMatrix.identity();
        this.ortho2DMatrix.setOrtho2D(
                left,
                right,
                bottom,
                top
        );
        return this.ortho2DMatrix;
    }
    
    public Matrix4f buildModelMatrix(final SceneElement sceneElement) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateY((float) Math.toRadians(-rotation.y))
                .rotateZ((float) Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        return this.modelMatrix;
    }

    public Matrix4f buildVIewModelMatrix(final SceneElement sceneElement,
                                         final Matrix4f viewMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        return buildVIewModelMatrix(this.modelMatrix, viewMatrix);
    }
    
    public Matrix4f buildVIewModelMatrix(final Matrix4f modelMatrix,
                                         final Matrix4f viewMatrix) {
        this.viewModelMatrix.set(viewMatrix);
        return this.viewModelMatrix.mul(modelMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(final SceneElement sceneElement,
                                              final Matrix4f matrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelLightMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        this.modelLightViewMatrix.set(matrix);
        return this.modelLightViewMatrix.mul(this.modelLightMatrix);
    }

    public Matrix4f buildOrthoProjModelMatrix(final SceneElement sceneElement,
                                              final Matrix4f orthoMatrix) {
        final Vector3f rotation = sceneElement.getRotation();
        this.modelMatrix.identity()
                .translate(sceneElement.getPosition())
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateY((float) Math.toRadians(-rotation.y))
                .rotateZ((float) Math.toRadians(-rotation.z))
                .scale(sceneElement.getScale());
        this.orthoModelMatrix.set(orthoMatrix);
        this.orthoModelMatrix.mul(this.modelMatrix);
        return this.orthoModelMatrix;
    }
}
