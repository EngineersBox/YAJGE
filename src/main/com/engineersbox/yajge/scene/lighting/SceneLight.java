package com.engineersbox.yajge.scene.lighting;

import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import org.joml.Vector3f;

public class SceneLight {

    private Vector3f ambientLight;
    private Vector3f skyBoxLight;
    private PointLight[] pointLightList;
    private SpotLight[] spotLightList;
    private DirectionalLight directionalLight;

    public Vector3f getAmbientLight() {
        return this.ambientLight;
    }

    public void setAmbientLight(final Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public PointLight[] getPointLightList() {
        return this.pointLightList;
    }

    public void setPointLightList(final PointLight[] pointLightList) {
        this.pointLightList = pointLightList;
    }

    public SpotLight[] getSpotLightList() {
        return this.spotLightList;
    }

    public void setSpotLightList(final SpotLight[] spotLightList) {
        this.spotLightList = spotLightList;
    }

    public DirectionalLight getDirectionalLight() {
        return this.directionalLight;
    }

    public void setDirectionalLight(final DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    public Vector3f getSkyBoxLight() {
        return this.skyBoxLight;
    }

    public void setSkyBoxLight(final Vector3f skyBoxLight) {
        this.skyBoxLight = skyBoxLight;
    }
    
}