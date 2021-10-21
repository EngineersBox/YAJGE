package com.engineersbox.yajge.scene.lighting;

import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.lighting.SpotLight;
import org.joml.Vector3f;

public class SceneLight {

    private Vector3f ambientLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;
    private DirectionalLight directionalLight;

    public Vector3f getAmbientLight() {
        return this.ambientLight;
    }

    public void setAmbientLight(final Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public PointLight[] getPointLights() {
        return this.pointLights;
    }

    public void setPointLights(final PointLight[] pointLights) {
        this.pointLights = pointLights;
    }

    public SpotLight[] getSpotLights() {
        return this.spotLights;
    }

    public void setSpotLights(final SpotLight[] spotLights) {
        this.spotLights = spotLights;
    }

    public DirectionalLight getDirectionalLight() {
        return this.directionalLight;
    }

    public void setDirectionalLight(final DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

}
