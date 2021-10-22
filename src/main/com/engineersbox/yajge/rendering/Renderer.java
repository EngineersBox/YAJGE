package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.lighting.SpotLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.assets.shader.Shader;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.ResourceLoader;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.gui.IHud;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.rendering.view.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private final Transform transform;
    private Shader sceneShader;
    private Shader hudShader;
    private Shader skyboxShader;
    private final float specularPower;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10f;
    }

    public void init(final Window window) {
        setupSkyBoxShader();
        initSceneShader();
        initHudShader();
    }

    private void initSceneShader() {
        this.sceneShader = new Shader();
        this.sceneShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.vert"));
        this.sceneShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.frag"));
        this.sceneShader.link();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "specularPower",
                "ambientLight"
        ).forEach(this.sceneShader::createUniform);
        this.sceneShader.createMaterialUniform("material");
        this.sceneShader.createPointLightListUniform("pointLights", ConfigHandler.CONFIG.render.lighting.maxPointLights);
        this.sceneShader.createSpotLightListUniform("spotLights", ConfigHandler.CONFIG.render.lighting.maxSpotLights);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
    }

    private void initHudShader() {
        this.hudShader = new Shader();
        this.hudShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/hud/hud.vert"));
        this.hudShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/hud/hud.frag"));
        this.hudShader.link();
        Stream.of(
                "projModelMatrix",
                "colour",
                "hasTexture"
        ).forEach(this.hudShader::createUniform);
    }

    private void setupSkyBoxShader() {
        this.skyboxShader = new Shader();
        this.skyboxShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.vert"));
        this.skyboxShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.frag"));
        this.skyboxShader.link();

        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "ambientLight"
        ).forEach(this.skyboxShader::createUniform);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene,
                       final IHud hud) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        this.transform.updateProjectionMatrix(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                window.getWidth(),
                window.getHeight(),
                (float) ConfigHandler.CONFIG.render.camera.zNear,
                (float) ConfigHandler.CONFIG.render.camera.zFar
        );
        this.transform.updateViewMatrix(camera);
        renderScene(scene);
        renderSkybox(scene);
        renderHud(window, hud);
    }

    private void renderSkybox(final Scene scene) {
        this.skyboxShader.bind();

        this.skyboxShader.setUniform("textureSampler", 0);

        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
        this.skyboxShader.setUniform("projectionMatrix", projectionMatrix);
        final Skybox skyBox = scene.getSkybox();
        final Matrix4f viewMatrix = this.transform.getViewMatrix();
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        final Matrix4f modelViewMatrix = this.transform.buildModelViewMatrix(skyBox, viewMatrix);
        this.skyboxShader.setUniform("viewModelMatrix", modelViewMatrix);
        this.skyboxShader.setUniform("ambientLight", scene.getSceneLight().getAmbientLight());

        scene.getSkybox().getMesh().render();
        this.skyboxShader.unbind();
    }

    public void renderScene(final Scene scene) {
        this.sceneShader.bind();

        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
        this.sceneShader.setUniform("projectionMatrix", projectionMatrix);

        final Matrix4f viewMatrix = this.transform.getViewMatrix();

        final SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        this.sceneShader.setUniform("textureSampler", 0);
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            this.sceneShader.setUniform("material", entry.getKey().getMaterial());
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement gameItem) -> {
                        final Matrix4f viewModelMatrix = this.transform.buildModelViewMatrix(gameItem, viewMatrix);
                        this.sceneShader.setUniform("viewModelMatrix", viewModelMatrix);
                    }
            );
        }

        this.sceneShader.unbind();
    }

    private void renderLights(final Matrix4f viewMatrix,
                              final SceneLight sceneLight) {

        this.sceneShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        this.sceneShader.setUniform("specularPower", specularPower);

        // Point Lights
        final PointLight[] pointLightList = sceneLight.getPointLights();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            final PointLight currPointLight = new PointLight(pointLightList[i]);
            final Vector3f lightPos = currPointLight.getPosition();
            final Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            this.sceneShader.setUniform("pointLights", currPointLight, i);
        }

        // Spotlights
        final SpotLight[] spotLightList = sceneLight.getSpotLights();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            final SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            final Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            final Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            final Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            this.sceneShader.setUniform("spotLights", currSpotLight, i);
        }

        final DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        final Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        this.sceneShader.setUniform("directionalLight", currDirLight);
    }

    private void renderHud(final Window window,
                           final IHud hud) {
        this.hudShader.bind();
        final Matrix4f orthoProjectionMatrix = this.transform.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (final SceneElement sceneElement : hud.getSceneElements()) {
            final Mesh mesh = sceneElement.getMesh();
            final Matrix4f projModelMatrix = this.transform.buildOrthoProjModelMatrix(sceneElement, orthoProjectionMatrix);
            this.hudShader.setUniform("projModelMatrix", projModelMatrix);
            this.hudShader.setUniform("colour", sceneElement.getMesh().getMaterial().getAmbientColour());
            this.hudShader.setUniform("hasTexture", sceneElement.getMesh().getMaterial().isTextured() ? 1 : 0);
            mesh.render();
        }
        this.hudShader.unbind();
    }

    public void cleanup() {
        if (this.skyboxShader != null) {
            this.skyboxShader.cleanup();
        }
        if (this.sceneShader != null) {
            this.sceneShader.cleanup();
        }
        if (this.hudShader != null) {
            this.hudShader.cleanup();
        }
    }
}
