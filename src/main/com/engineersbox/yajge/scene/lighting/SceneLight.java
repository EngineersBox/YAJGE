package com.engineersbox.yajge.scene.lighting;

import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Vector3f;

public class SceneLight {

    private Vector3f ambientLight;
    private Vector3f skyboxLight;
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

    public void addPointLight(final PointLight pointLight) {
        this.pointLights = ArrayUtils.add(this.pointLights, pointLight);
    }

    public void setPointLights(final PointLight[] pointLights) {
        this.pointLights = pointLights;
    }

    public SpotLight[] getSpotLights() {
        return this.spotLights;
    }

    public void addSpotLight(final SpotLight spotLight) {
        this.spotLights = ArrayUtils.add(this.spotLights, spotLight);
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

    public void setSkyboxLight(final Vector3f skyboxLight) {
        this.skyboxLight = skyboxLight;
    }
    
}