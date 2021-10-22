package com.engineersbox.yajge.scene;

import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.lighting.SceneLight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    private final Map<Mesh, List<SceneElement>> meshSceneElements;
    private Skybox skyBox;
    private SceneLight sceneLight;

    public Scene() {
        this.meshSceneElements = new HashMap<>();
    }

    public Map<Mesh, List<SceneElement>> getMeshSceneElements() {
        return this.meshSceneElements;
    }

    public void setSceneElements(final SceneElement[] sceneElements) {
        if (sceneElements == null) {
            return;
        }
        for (final SceneElement sceneElement : sceneElements) {
            final Mesh mesh = sceneElement.getMesh();
            final List<SceneElement> list = this.meshSceneElements.computeIfAbsent(mesh, (final Mesh key) -> new ArrayList<>());
            list.add(sceneElement);
        }
    }

    public Skybox getSkybox() {
        return this.skyBox;
    }

    public void setSkybox(final Skybox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return this.sceneLight;
    }

    public void setSceneLight(final SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}
