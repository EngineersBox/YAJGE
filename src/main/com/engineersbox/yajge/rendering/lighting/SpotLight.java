package com.engineersbox.yajge.rendering.lighting;

import org.joml.Vector3f;

public class SpotLight {

    private PointLight pointLight;
    private Vector3f coneDirection;
    private float cutOff;

    public SpotLight(final PointLight pointLight,
                     final Vector3f coneDirection,
                     final float cutOffAngle) {
        this.pointLight = pointLight;
        this.coneDirection = coneDirection;
        setCutOffAngle(cutOffAngle);
    }

    public SpotLight(final SpotLight spotLight) {
        this(
                new PointLight(spotLight.getPointLight()),
                new Vector3f(spotLight.getConeDirection()),
                0
        );
        setCutOff(spotLight.getCutOff());
    }

    public PointLight getPointLight() {
        return this.pointLight;
    }

    public void setPointLight(final PointLight pointLight) {
        this.pointLight = pointLight;
    }

    public Vector3f getConeDirection() {
        return this.coneDirection;
    }

    public void setConeDirection(final Vector3f coneDirection) {
        this.coneDirection = coneDirection;
    }

    public float getCutOff() {
        return this.cutOff;
    }

    public void setCutOff(final float cutOff) {
        this.cutOff = cutOff;
    }

    public final void setCutOffAngle(final float cutOffAngle) {
        this.setCutOff((float) Math.cos(Math.toRadians(cutOffAngle)));
    }

}
