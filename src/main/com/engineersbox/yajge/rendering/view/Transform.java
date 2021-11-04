package com.engineersbox.yajge.rendering.view;

import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {

    private final Matrix4f modelMatrix;
    private final Matrix4f viewModelMatrix;
    private final Matrix4f modelLightViewMatrix;
    private final Matrix4f lightViewMatrix;
    private final Matrix4f orthoProjMatrix;
    private final Matrix4f ortho2DMatrix;
    private final Matrix4f orthoModelMatrix;

    public Transform() {
        this.modelMatrix = new Matrix4f();
        this.viewModelMatrix = new Matrix4f();
        this.modelLightViewMatrix = new Matrix4f();
        this.orthoProjMatrix = new Matrix4f();
        this.ortho2DMatrix = new Matrix4f();
        this.orthoModelMatrix = new Matrix4f();
        this.lightViewMatrix = new Matrix4f();
    }

    public final Matrix4f getOrthoProjectionMatrix() {
        return this.orthoProjMatrix;
    }

    public Matrix4f updateOrthoProjectionMatrix(final OrthoCoords orthoCoords) {
        return updateOrthoProjectionMatrix(
                orthoCoords.left,
                orthoCoords.right,
                orthoCoords.bottom,
                orthoCoords.top,
                orthoCoords.near,
                orthoCoords.far
        );
    }

    public Matrix4f updateOrthoProjectionMatrix(final float left,
                                                final float right,
                                                final float bottom,
                                                final float top,
                                                final float zNear,
                                                final float zFar) {
        return this.orthoProjMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
    }

    public Matrix4f getLightViewMatrix() {
        return this.lightViewMatrix;
    }

    public void setLightViewMatrix(final Matrix4f lightViewMatrix) {
        this.lightViewMatrix.set(lightViewMatrix);
    }

    public Matrix4f updateLightViewMatrix(final Vector3f position,
                                          final Vector3f rotation) {
        return updateGenericViewMatrix(position, rotation, this.lightViewMatrix);
    }

    public static Matrix4f updateGenericViewMatrix(final Vector3f position,
                                                   final Vector3f rotation,
                                                   final Matrix4f matrix) {
        return matrix.rotationX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .translate(
                        -position.x,
                        -position.y,
                        -position.z
                );
    }

    public final Matrix4f getOrtho2DProjectionMatrix(final float left,
                                                     final float right,
                                                     final float bottom,
                                                     final float top) {
        return this.ortho2DMatrix.setOrtho2D(left, right, bottom, top);
    }

    public Matrix4f buildModelMatrix(final SceneElement sceneElement) {
        final Quaternionf rotation = sceneElement.getRotation();
        return this.modelMatrix.translationRotateScale(
                sceneElement.getPosition().x, sceneElement.getPosition().y, sceneElement.getPosition().z,
                rotation.x, rotation.y, rotation.z, rotation.w,
                sceneElement.getScale(), sceneElement.getScale(), sceneElement.getScale()
        );
    }

    public Matrix4f buildViewModelMatrix(final SceneElement sceneElement,
                                         final Matrix4f viewMatrix) {
        return buildViewModelMatrix(buildModelMatrix(sceneElement), viewMatrix);
    }

    public Matrix4f buildViewModelMatrix(final Matrix4f modelMatrix,
                                         final Matrix4f viewMatrix) {
        return viewMatrix.mulAffine(modelMatrix, this.viewModelMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(final SceneElement sceneElement,
                                              final Matrix4f lightViewMatrix) {
        return buildViewModelMatrix(buildModelMatrix(sceneElement), lightViewMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(final Matrix4f modelMatrix,
                                              final Matrix4f lightViewMatrix) {
        return lightViewMatrix.mulAffine(modelMatrix, this.modelLightViewMatrix);
    }

    public Matrix4f buildOrthoProjModelMatrix(final SceneElement sceneElement,
                                              final Matrix4f orthoMatrix) {
        return orthoMatrix.mulOrthoAffine(buildModelMatrix(sceneElement), this.orthoModelMatrix);
    }
}
