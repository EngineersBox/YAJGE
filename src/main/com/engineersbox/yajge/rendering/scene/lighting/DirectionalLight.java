package com.engineersbox.yajge.rendering.scene.lighting;

import com.engineersbox.yajge.rendering.view.OrthoCoords;
import org.joml.Vector3f;

public class DirectionalLight {
    
    private Vector3f color;
    private Vector3f direction;
    private float intensity;
    private final OrthoCoords orthoCords;
    private float shadowPosMult;
    
    public DirectionalLight(final Vector3f color,
                            final Vector3f direction,
                            final float intensity) {
        this.orthoCords = new OrthoCoords();
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
        this.shadowPosMult = 1;
    }

    public DirectionalLight(final DirectionalLight light) {
        this(
                new Vector3f(light.getColor()),
                new Vector3f(light.getDirection()),
                light.getIntensity()
        );
    }

    public float getShadowPosMult() {
        return this.shadowPosMult;
    }
    
    public void setShadowPosMult(final float shadowPosMult) {
        this.shadowPosMult = shadowPosMult;
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