package com.engineersbox.yajge.rendering.scene.lighting;

import org.joml.Vector3f;

public interface ILightFixture {

    Vector3f getColor();
    void setColor(final Vector3f color);
    Vector3f getPosition();
    void setPosition(final Vector3f position);
    float getIntensity();
    void setIntensity(final float intensity);
}
