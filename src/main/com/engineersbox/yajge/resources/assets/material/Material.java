package com.engineersbox.yajge.resources.assets.material;

import org.joml.Vector4f;

public class Material {

    public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColour;
    private Vector4f diffuseColour;
    private Vector4f specularColour;
    private float reflectance;
    private Texture texture;
    private Texture normalMap;

    public Material() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = DEFAULT_COLOUR;
        this.specularColour = DEFAULT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
    }

    public Material(final Vector4f colour,
                    final float reflectance) {
        this(colour, colour, colour, null, reflectance);
    }

    public Material(final Texture texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
    }

    public Material(final Texture texture,
                    final float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
    }

    public Material(final Vector4f ambientColour,
                    final Vector4f diffuseColour,
                    final Vector4f specularColour,
                    final float reflectance) {
        this(ambientColour, diffuseColour, specularColour, null, reflectance);
    }

    public Material(final Vector4f ambientColour,
                    final Vector4f diffuseColour,
                    final Vector4f specularColour,
                    final Texture texture,
                    final float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = texture;
        this.reflectance = reflectance;
    }

    public Vector4f getAmbientColour() {
        return this.ambientColour;
    }

    public void setAmbientColour(final Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }

    public Vector4f getDiffuseColour() {
        return this.diffuseColour;
    }

    public void setDiffuseColour(final Vector4f diffuseColour) {
        this.diffuseColour = diffuseColour;
    }

    public Vector4f getSpecularColour() {
        return this.specularColour;
    }

    public void setSpecularColour(final Vector4f specularColour) {
        this.specularColour = specularColour;
    }

    public float getReflectance() {
        return this.reflectance;
    }

    public void setReflectance(final float reflectance) {
        this.reflectance = reflectance;
    }

    public boolean isTextured() {
        return this.texture != null;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(final Texture texture) {
        this.texture = texture;
    }

    public boolean hasNormalMap() {
        return this.normalMap != null;
    }

    public Texture getNormalMap() {
        return this.normalMap;
    }

    public void setNormalMap(final Texture normalMap) {
        this.normalMap = normalMap;
    }
}