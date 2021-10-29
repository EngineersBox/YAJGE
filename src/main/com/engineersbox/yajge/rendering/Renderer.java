package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.rendering.view.OrthoCoords;
import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.assets.shader.Shader;
import com.engineersbox.yajge.resources.assets.shader.ShadowMap;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajge.scene.gui.IHud;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class Renderer {

    private final Transform transform;
    private ShadowMap shadowMap;
    private Shader depthShader;
    private Shader sceneShader;
    private Shader hudShader;
    private Shader skyBoxShader;
    private Shader particlesShader;
    private final float specularPower;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10f;
    }

    public void init(final Window window) {
        this.shadowMap = new ShadowMap();

        setupDepthShader();
        setupSkyBoxShader();
        setupSceneShader();
        setupParticlesShader();
        setupHudShader();
    }

    public void render(final Window window, final Camera camera, final Scene scene, final IHud hud) {
        clear();
        renderDepthMap(scene);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        this.transform.updateProjectionMatrix(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                window.getWidth(),
                window.getHeight(),
                (float) ConfigHandler.CONFIG.render.camera.zNear,
                (float) ConfigHandler.CONFIG.render.camera.zFar
        );
        this.transform.updateViewMatrix(camera);

        renderScene(scene);
        renderSkyBox(scene);
        renderParticles(scene);
        renderHud(window, hud);

        //renderAxes(camera);
    }

    private void setupParticlesShader() {
        this.particlesShader = new Shader();
        this.particlesShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.vert"));
        this.particlesShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.frag"));
        this.particlesShader.link();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "texXOffset",
                "texYOffset",
                "cols",
                "rows"
        ).forEach(this.particlesShader::createUniform);
    }

    private void setupDepthShader() {
        this.depthShader = new Shader();
        this.depthShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.vert"));
        this.depthShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.frag"));
        this.depthShader.link();
        Stream.of(
                "orthoProjectionMatrix",
                "modelLightViewMatrix",
                "jointsMatrix"
        ).forEach(this.depthShader::createUniform);
    }

    private void setupSkyBoxShader() {
        this.skyBoxShader = new Shader();
        this.skyBoxShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.vert"));
        this.skyBoxShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.frag"));
        this.skyBoxShader.link();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "ambientLight"
        ).forEach(this.skyBoxShader::createUniform);
    }

    private void setupSceneShader() {
        // Create shader
        this.sceneShader = new Shader();
        this.sceneShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.vert"));
        this.sceneShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.frag"));
        this.sceneShader.link();
        this.sceneShader.createMaterialUniform("material");
        this.sceneShader.createPointLightListUniform("pointLights", ConfigHandler.CONFIG.render.lighting.maxPointLights);
        this.sceneShader.createSpotLightListUniform("spotLights", ConfigHandler.CONFIG.render.lighting.maxSpotLights);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
        this.sceneShader.createFogUniform("fog");
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "normalMap",
                "specularPower",
                "ambientLight",
                "shadowMap",
                "orthoProjectionMatrix",
                "modelLightViewMatrix",
                "jointsMatrix"
        ).forEach(this.sceneShader::createUniform);
    }

    private void setupHudShader() {
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

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void renderParticles(final Scene scene) {
        this.particlesShader.bind();

        this.particlesShader.setUniform("textureSampler", 0);
        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
        this.particlesShader.setUniform("projectionMatrix", projectionMatrix);

        final Matrix4f viewMatrix = this.transform.getViewMatrix();
        final IParticleEmitter[] emitters = scene.getParticleEmitters();
        final int numEmitters = emitters != null ? emitters.length : 0;

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (int i = 0; i < numEmitters; i++) {
            final IParticleEmitter emitter = emitters[i];
            final Mesh mesh = emitter.getBaseParticle().getMesh();

            final Texture text = mesh.getMaterial().getTexture();
            this.particlesShader.setUniform("cols", text.getCols());
            this.particlesShader.setUniform("rows", text.getRows());

            mesh.renderList(
                    emitter.getParticles(),
                    (final SceneElement sceneElement) -> {
                        final int col = sceneElement.getTextPos() % text.getCols();
                        final int row = sceneElement.getTextPos() / text.getCols();
                        final float textXOffset = (float) col / text.getCols();
                        final float textYOffset = (float) row / text.getRows();
                        this.particlesShader.setUniform("texXOffset", textXOffset);
                        this.particlesShader.setUniform("texYOffset", textYOffset);

                        final Matrix4f modelMatrix = this.transform.buildModelMatrix(sceneElement);

                        viewMatrix.transpose3x3(modelMatrix);
                        viewMatrix.scale(sceneElement.getScale());

                        final Matrix4f viewModelMatrix = this.transform.buildVIewModelMatrix(modelMatrix, viewMatrix);
                        viewModelMatrix.scale(sceneElement.getScale());
                        this.particlesShader.setUniform("viewModelMatrix", viewModelMatrix);
                    }
            );
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        this.particlesShader.unbind();
    }

    private void renderDepthMap(final Scene scene) {
        glBindFramebuffer(GL_FRAMEBUFFER, this.shadowMap.getDepthMapFBO());
        glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);

        this.depthShader.bind();

        final DirectionalLight light = scene.getSceneLight().getDirectionalLight();
        final Vector3f lightDirection = light.getDirection();

        final float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
        final float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
        final float lightAngleZ = 0;
        final Matrix4f lightViewMatrix = this.transform.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX, lightAngleY, lightAngleZ));
        final OrthoCoords orthCoords = light.getOrthoCoords();
        final Matrix4f orthoProjMatrix = this.transform.updateOrthoProjectionMatrix(orthCoords.left, orthCoords.right, orthCoords.bottom, orthCoords.top, orthCoords.near, orthCoords.far);

        this.depthShader.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        final Matrix4f modelLightViewMatrix = this.transform.buildVIewModelMatrix(sceneElement, lightViewMatrix);
                        this.depthShader.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            this.depthShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
        this.depthShader.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderSkyBox(final Scene scene) {
        final Skybox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            this.skyBoxShader.bind();

            this.skyBoxShader.setUniform("textureSampler", 0);

            final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
            this.skyBoxShader.setUniform("projectionMatrix", projectionMatrix);
            final Matrix4f viewMatrix = this.transform.getViewMatrix();
            final float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            final float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            final float m32 = viewMatrix.m32();
            viewMatrix.m32(0);

            final Matrix4f viewModelMatrix = this.transform.buildVIewModelMatrix(skyBox, viewMatrix);
            this.skyBoxShader.setUniform("viewModelMatrix", viewModelMatrix);
            this.skyBoxShader.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());

            scene.getSkyBox().getMesh().render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            this.skyBoxShader.unbind();
        }
    }

    public void renderScene(final Scene scene) {
        this.sceneShader.bind();

        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix();
        this.sceneShader.setUniform("projectionMatrix", projectionMatrix);
        final Matrix4f orthoProjMatrix = this.transform.getOrthoProjectionMatrix();
        this.sceneShader.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        final Matrix4f lightViewMatrix = this.transform.getLightViewMatrix();

        final Matrix4f viewMatrix = this.transform.getViewMatrix();

        final SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        this.sceneShader.setUniform("fog", scene.getFog());
        this.sceneShader.setUniform("textureSampler", 0);
        this.sceneShader.setUniform("normalMap", 1);
        this.sceneShader.setUniform("shadowMap", 2);

        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            this.sceneShader.setUniform("material", entry.getKey().getMaterial());
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, this.shadowMap.getDepthMapTexture().getId());
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        this.sceneShader.setUniform("viewModelMatrix", this.transform.buildVIewModelMatrix(sceneElement, viewMatrix));
                        this.sceneShader.setUniform("modelLightViewMatrix", this.transform.buildModelLightViewMatrix(sceneElement, lightViewMatrix));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            this.sceneShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }

        this.sceneShader.unbind();
    }

    private void renderLights(final Matrix4f viewMatrix, final SceneLight sceneLight) {

        this.sceneShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        this.sceneShader.setUniform("specularPower", this.specularPower);

        final PointLight[] pointLightList = sceneLight.getPointLightList();
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

        final SpotLight[] spotLightList = sceneLight.getSpotLightList();
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

    private void renderHud(final Window window, final IHud hud) {
        if (hud != null) {
            this.hudShader.bind();

            final Matrix4f ortho = this.transform.getOrtho2DProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
            for (final SceneElement sceneElement : hud.getGameItems()) {
                final Mesh mesh = sceneElement.getMesh();
                final Matrix4f projModelMatrix = this.transform.buildOrthoProjModelMatrix(sceneElement, ortho);
                this.hudShader.setUniform("projModelMatrix", projModelMatrix);
                this.hudShader.setUniform("colour", sceneElement.getMesh().getMaterial().getAmbientColour());
                this.hudShader.setUniform("hasTexture", sceneElement.getMesh().getMaterial().isTextured() ? 1 : 0);

                mesh.render();
            }

            this.hudShader.unbind();
        }
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
        if (this.skyBoxShader != null) {
            this.skyBoxShader.cleanup();
        }
        if (this.sceneShader != null) {
            this.sceneShader.cleanup();
        }
        if (this.hudShader != null) {
            this.hudShader.cleanup();
        }
        if (this.particlesShader != null) {
            this.particlesShader.cleanup();
        }
    }
}
