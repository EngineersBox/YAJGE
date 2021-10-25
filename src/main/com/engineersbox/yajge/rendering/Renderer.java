package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajge.rendering.object.composite.Mesh;
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
import com.engineersbox.yajge.scene.lighting.ShadowMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private final Transform transform;
    private Shader sceneShader;
    private Shader hudShader;
    private Shader skyboxShader;
    private Shader depthShader;
    private final float specularPower;
    private ShadowMap shadowMap;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10f;
    }

    public void init(final Window window) {
        this.shadowMap = new ShadowMap();
        configureDepthShader();
        configureSkyBoxShader();
        configureSceneShader();
        configureHudShader();
    }

    private void configureSceneShader() {
        this.sceneShader = new Shader();
        this.sceneShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.vert"));
        this.sceneShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.frag"));
        this.sceneShader.link();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "specularPower",
                "ambientLight",
                "normalMap"
        ).forEach(this.sceneShader::createUniform);
        this.sceneShader.createMaterialUniform("material");
        this.sceneShader.createPointLightListUniform("pointLights", ConfigHandler.CONFIG.render.lighting.maxPointLights);
        this.sceneShader.createSpotLightListUniform("spotLights", ConfigHandler.CONFIG.render.lighting.maxSpotLights);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
        this.sceneShader.createFogUniform("fog");
        this.sceneShader.createUniform("shadowMap");
        this.sceneShader.createUniform("orthoProjectionMatrix");
        this.sceneShader.createUniform("modelLightViewMatrix");
    }

    private void configureHudShader() {
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

    private void configureSkyBoxShader() {
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

    private void configureDepthShader() {
        this.depthShader = new Shader();
        this.depthShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.vert"));
        this.depthShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.frag"));
        this.depthShader.link();
        this.depthShader.createUniform("orthoProjectionMatrix");
        this.depthShader.createUniform("modelLightViewMatrix");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene,
                       final IHud hud) {
        clear();
        renderDepthMap(window, camera, scene);
        glViewport(
                0,
                0,
                window.getWidth(),
                window.getHeight()
        );
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

    private void renderDepthMap(final Window window,
                                final Camera camera,
                                final Scene scene) {
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        glViewport(
                0,
                0,
                ConfigHandler.CONFIG.render.lighting.shadowMapWidth,
                ConfigHandler.CONFIG.render.lighting.shadowMapHeight
        );
        glClear(GL_DEPTH_BUFFER_BIT);
        this.depthShader.bind();

        final DirectionalLight light = scene.getSceneLight().getDirectionalLight();
        final Vector3f lightDirection = light.getDirection();

        final float lightAngleX = (float)Math.toDegrees(Math.acos(lightDirection.z));
        final float lightAngleY = (float)Math.toDegrees(Math.asin(lightDirection.x));
        final float lightAngleZ = 0;
        final Matrix4f lightViewMatrix = this.transform.updateLightViewMatrix(
                new Vector3f(lightDirection).mul(light.getShadowPosMultiplier()),
                new Vector3f(lightAngleX, lightAngleY, lightAngleZ)
        );
        final Matrix4f orthoProjMatrix = this.transform.updateOrthoProjectionMatrix(light.getOrthoCoords());
        this.depthShader.setUniform("orthoProjectionMatrix", orthoProjMatrix);

        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement gameItem) -> {
                        final Matrix4f modelLightViewMatrix = this.transform.buildViewModelMatrix(gameItem, lightViewMatrix);
                        this.depthShader.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                    }
            );
        }
        this.depthShader.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderSkybox(final Scene scene) {
        final Skybox skybox = scene.getSkybox();
        if (skybox == null) {
            return;
        }
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("textureSampler", 0);

        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
        this.skyboxShader.setUniform("projectionMatrix", projectionMatrix);
        final Matrix4f viewMatrix = this.transform.getViewMatrix();
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);

        final Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(skybox, viewMatrix);
        this.skyboxShader.setUniform("viewModelMatrix", viewModelMatrix);
        this.skyboxShader.setUniform("ambientLight", scene.getSceneLight().getSkyboxLight());

        skybox.getMesh().render();
        this.skyboxShader.unbind();
    }

    public void renderScene(final Scene scene) {
        this.sceneShader.bind();
        this.sceneShader.setUniform("projectionMatrix", this.transform.getProjectionMatrix());
        this.sceneShader.setUniform("orthoProjectionMatrix", this.transform.getOrthoProjectionMatrix());
        final Matrix4f viewMatrix = this.transform.getViewMatrix();
        renderLights(viewMatrix, scene.getSceneLight());

        this.sceneShader.setUniform("fog", scene.getFog());
        this.sceneShader.setUniform("textureSampler", 0);
        this.sceneShader.setUniform("normalMap", 1);
        this.sceneShader.setUniform("shadowMap", 2);

        // Render each mesh with the associated game Items
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            final Mesh mesh = entry.getKey();
            this.sceneShader.setUniform("material", mesh.getMaterial());
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
            mesh.renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(sceneElement, viewMatrix);
                        this.sceneShader.setUniform("viewModelMatrix", viewModelMatrix);
                        Matrix4f modelLightViewMatrix = this.transform.buildModelLightViewMatrix(sceneElement, this.transform.getLightViewMatrix());
                        this.sceneShader.setUniform("modelLightViewMatrix", modelLightViewMatrix);
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
        final Matrix4f orthoProjectionMatrix = this.transform.getOrtho2DProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
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

    private void renderAxes(final Camera camera) {
        glPushMatrix();
        glLoadIdentity();
        final float rotX = camera.getRotation().x;
        final float rotY = camera.getRotation().y;
        final float rotZ = 0;
        glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
        glLineWidth(2.0f);

        glBegin(GL_LINES);
        // X Axis
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(1.0f, 0.0f, 0.0f);
        // Y Axis
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 1.0f, 0.0f);
        // Z Axis
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 1.0f);
        glEnd();

        glPopMatrix();
    }

    public void cleanup() {
        if (this.shadowMap != null) {
            this.shadowMap.cleanup();
        }
        if (this.depthShader != null) {
            this.depthShader.cleanup();
        }
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
