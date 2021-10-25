package com.engineersbox.yajge.rendering.scene.lighting;

import com.engineersbox.yajge.rendering.view.OrthoCoords;
import org.joml.Vector3f;

public class DirectionalLight {

    private Vector3f color;
    private Vector3f direction;
    private float intensity;
    private OrthoCoords orthoCords;
    private float shadowPosMultiplier;

    public DirectionalLight(final Vector3f color,
                            final Vector3f direction,
                            final float intensity) {
        this.orthoCords = new OrthoCoords();
        this.shadowPosMultiplier = 1;
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }

    public DirectionalLight(final DirectionalLight light) {
        this(
                new Vector3f(light.getColor()),
                new Vector3f(light.getDirection()),
                light.getIntensity()
        );
    }

    public float getShadowPosMultiplier() {
        return shadowPosMultiplier;
    }

    public void setShadowPosMultiplier(final float shadowPosMultiplier) {
        this.shadowPosMultiplier = shadowPosMultiplier;
    }

    public OrthoCoords getOrthoCoords(){
        return this.orthoCords;
    }

    public void setOrthoCords(final float left,
                              final float right,
                              final float bottom,
                              final float top,
                              final float near,
                              final float far) {
        this.orthoCords.left = left;
        this.orthoCords.right = right;
        this.orthoCords.bottom = bottom;
        this.orthoCords.top = top;
        this.orthoCords.near = near;
        this.orthoCords.far = far;
    }

    public void setOrthoCoords(final OrthoCoords orthoCords) {
        this.orthoCords = orthoCords;
    }

    public Vector3f getColor() {
        return this.color;
    }

    public void setColor(final Vector3f color) {
        this.color = color;
    }

    public Vector3f getDirection() {
        return this.direction;
    }

    public void setDirection(final Vector3f direction) {
        this.direction = direction;
    }

    public float getIntensity() {
        return this.intensity;
    }

    public void setIntensity(final float intensity) {
        this.intensity = intensity;
    }
}
