package com.engineersbox.yajge.scene;

import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.lighting.SceneLight;

public class Scene {

    private SceneElement[] sceneElements;
    private Skybox skybox;
    private SceneLight sceneLight;

    public SceneElement[] getSceneElements() {
        return this.sceneElements;
    }

    public void setSceneElements(final SceneElement[] sceneElements) {
        this.sceneElements = sceneElements;
    }

    public Skybox getSkybox() {
        return this.skybox;
    }

    public void setSkyBox(final Skybox skybox) {
        this.skybox = skybox;
    }

    public SceneLight getSceneLight() {
        return this.sceneLight;
    }

    public void setSceneLight(final SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}
