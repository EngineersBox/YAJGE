package com.engineersbox.yajge.scene;

import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajge.scene.lighting.SceneLight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    private final Map<Mesh, List<SceneElement>> meshSceneElements;
    private Skybox skyBox;
    private SceneLight sceneLight;
    private Fog fog;
    private IParticleEmitter[] particleEmitters;

    public Scene() {
        this.meshSceneElements = new HashMap<>();
        this.fog = Fog.NOFOG;
    }

    public Map<Mesh, List<SceneElement>> getMeshSceneElements() {
        return this.meshSceneElements;
    }

    public void getSceneElements(final SceneElement[] sceneElements) {
        if (sceneElements == null) {
            return;
        }
        for (final SceneElement sceneElement : sceneElements) {
            final Mesh mesh = sceneElement.getMesh();
            final List<SceneElement> list = this.meshSceneElements.computeIfAbsent(mesh, (final Mesh key) -> new ArrayList<>());
            list.add(sceneElement);
        }
    }

    public void cleanup() {
        for (final Mesh mesh : this.meshSceneElements.keySet()) {
            mesh.cleanUp();
        }
        for (final IParticleEmitter particleEmitter : this.particleEmitters) {
            particleEmitter.cleanup();            
        }
    }

    public Skybox getSkyBox() {
        return this.skyBox;
    }

    public void setSkyBox(final Skybox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return this.sceneLight;
    }

    public void setSceneLight(final SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }

    public Fog getFog() {
        return this.fog;
    }

    public void setFog(final Fog fog) {
        this.fog = fog;
    }

    public IParticleEmitter[] getParticleEmitters() {
        return this.particleEmitters;
    }

    public void setParticleEmitters(final IParticleEmitter[] particleEmitters) {
        this.particleEmitters = particleEmitters;
    }

}
