package com.engineersbox.yajge.rendering.scene.atmosphere;

import org.joml.Vector3f;

public class Fog {

    private boolean active;
    private Vector3f colour;
    private float density;
    public static final Fog NO_FOG = new Fog();

    public Fog() {
        this.active = false;
        this.colour = new Vector3f();
        this.density = 0;
    }

    public Fog(final boolean active,
               final Vector3f colour,
               final float density) {
        this.colour = colour;
        this.density = density;
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Vector3f getColour() {
        return this.colour;
    }

    public void setColour(final Vector3f colour) {
        this.colour = colour;
    }

    public float getDensity() {
        return this.density;
    }

    public void setDensity(final float density) {
        this.density = density;
    }
}