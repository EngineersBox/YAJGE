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

    private Map<Mesh, List<SceneElement>> meshMap;

    private Skybox skyBox;

    private SceneLight sceneLight;

    public Scene() {
        this.meshMap = new HashMap();
    }

    public Map<Mesh, List<SceneElement>> getGameMeshes() {
        return this.meshMap;
    }

    public void setSceneElements(final SceneElement[] sceneElements) {
        final int elementCount = sceneElements != null ? sceneElements.length : 0;
        for (int i = 0; i < elementCount; i++) {
            final SceneElement sceneElement = sceneElements[i];
            final Mesh mesh = sceneElement.getMesh();
            final List<SceneElement> list = this.meshMap.computeIfAbsent(mesh, k -> new ArrayList<>());
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
