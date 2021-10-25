package com.engineersbox.yajge.scene.lighting;

import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import org.joml.Vector3f;

public class SceneLight {

    private Vector3f ambientLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;
    private DirectionalLight directionalLight;
    private Vector3f skyboxLight;

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

    public Vector3f getSkyboxLight() {
        return this.skyboxLight;
    }

    public void setSkyboxLight(Vector3f skyboxLight) {
        this.skyboxLight = skyboxLight;
    }

}
