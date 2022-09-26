package com.engineersbox.yajge.scene;

import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajge.scene.lighting.SceneLight;

import java.util.*;

public class Scene {

    private final Map<Mesh, List<SceneElement>> nonInstancedMeshes;
    private final Map<InstancedMesh, List<SceneElement>> instancedMeshes;
    private Skybox skyBox;
    private SceneLight sceneLight;
    private Fog fog;
    private boolean renderShadows;
    private IParticleEmitter[] particleEmitters;

    public Scene() {
        this.nonInstancedMeshes = new HashMap<>();
        this.instancedMeshes = new HashMap<>();
        this.fog = Fog.NO_FOG;
        this.renderShadows = true;
    }

    public Map<Mesh, List<SceneElement>> getNonInstancedMeshes() {
        return this.nonInstancedMeshes;
    }

    public Map<InstancedMesh, List<SceneElement>> getInstancedMeshes() {
        return this.instancedMeshes;
    }

    public boolean isRenderShadows() {
        return this.renderShadows;
    }

    public void setSceneElements(final SceneElement[] sceneElements) {
        if (sceneElements == null) {
            return;
        }
        for (final SceneElement sceneElement : sceneElements) {
            final Mesh[] meshes = sceneElement.getMeshes();
            for (final Mesh mesh : meshes) {
                final boolean instancedMesh = mesh instanceof InstancedMesh;
                List<SceneElement> list = instancedMesh ? this.instancedMeshes.get(mesh) : this.nonInstancedMeshes.get(mesh);
                if (list == null) {
                    list = new ArrayList<>();
                    if (instancedMesh) {
                        this.instancedMeshes.put((InstancedMesh) mesh, list);
                    } else {
                        this.nonInstancedMeshes.put(mesh, list);
                    }
                }
                list.add(sceneElement);
            }
        }
    }

    public void cleanup() {
        this.nonInstancedMeshes.keySet().forEach(Mesh::cleanUp);
        this.instancedMeshes.keySet().forEach(Mesh::cleanUp);
        if (this.particleEmitters != null) {
            Arrays.stream(this.particleEmitters).forEach(IParticleEmitter::cleanup);
        }
    }

    public Skybox getSkybox() {
        return this.skyBox;
    }

    public void setRenderShadows(final boolean renderShadows) {
        this.renderShadows = renderShadows;
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
