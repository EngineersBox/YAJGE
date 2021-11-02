package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajge.rendering.view.Camera;
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
import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
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

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
    
    private final Transform transform;
    private ShadowMap shadowMap;
    private Shader depthShader;
    private Shader sceneShader;
    private Shader hudShader;
    private Shader skyboxShader;
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

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene,
                       final IHud hud) {
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
    }

    private void setupParticlesShader() {
        this.particlesShader = new Shader();
        this.particlesShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.vert"));
        this.particlesShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.frag"));
        this.particlesShader.link();

        Stream.of(
                "projectionMatrix",
                "textureSampler",
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
                "isInstanced",
                "jointsMatrix",
                "modelLightViewNonInstancedMatrix",
                "orthoProjectionMatrix"
        ).forEach(this.depthShader::createUniform);
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
                "ambientLight",
                "colour",
                "hasTexture"
        ).forEach(this.skyboxShader::createUniform);
    }

    private void setupSceneShader() {
        this.sceneShader = new Shader();
        this.sceneShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.vert"));
        this.sceneShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/final.frag"));
        this.sceneShader.link();

        this.sceneShader.createMaterialUniform("material");
        this.sceneShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        this.sceneShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
        this.sceneShader.createFogUniform("fog");
        Stream.of(
                "projectionMatrix",
                "viewModelNonInstancedMatrix",
                "textureSampler",
                "specularPower",
                "ambientLight",
                "shadowMap",
                "normalMap",
                "orthoProjectionMatrix",
                "modelLightViewNonInstancedMatrix",
                "renderShadow",
                "jointsMatrix",
                "isInstanced",
                "rows",
                "cols"
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
        if (emitters == null) {
            this.particlesShader.unbind();
            return;
        }

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (final IParticleEmitter emitter : emitters) {
            final InstancedMesh mesh = (InstancedMesh)emitter.getBaseParticle().getMesh();

            final Texture text = mesh.getMaterial().getTexture();
            this.particlesShader.setUniform("cols", text.getCols());
            this.particlesShader.setUniform("rows", text.getRows());

            mesh.renderListInstanced(emitter.getParticles(), true, this.transform, viewMatrix, null);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        this.particlesShader.unbind();
    }

    private void renderDepthMap(final Scene scene) {
        if (!scene.isRenderShadows()) {
            return;
        }
        glBindFramebuffer(GL_FRAMEBUFFER, this.shadowMap.getDepthMapFBO());
        glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);
        this.depthShader.bind();

        final DirectionalLight light = scene.getSceneLight().getDirectionalLight();
        final Vector3f lightDirection = light.getDirection();

        final Matrix4f lightViewMatrix = this.transform.updateLightViewMatrix(
                new Vector3f(lightDirection).mul(light.getShadowPosMult()),
                new Vector3f(
                        (float) Math.toDegrees(Math.acos(lightDirection.z)),
                        (float) Math.toDegrees(Math.asin(lightDirection.x)),
                        0
                )
        );
        this.depthShader.setUniform("orthoProjectionMatrix", this.transform.updateOrthoProjectionMatrix(light.getOrthoCoords()));

        renderNonInstancedMeshes(scene, this.depthShader, null, lightViewMatrix);
        renderInstancedMeshes(scene, this.depthShader, null, lightViewMatrix);

        this.depthShader.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderSkyBox(final Scene scene) {
        final Skybox skybox = scene.getSkybox();
        if (skybox == null) {
            return;
        }
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("textureSampler", 0);
        this.skyboxShader.setUniform("projectionMatrix", this.transform.getProjectionMatrix());
        final Matrix4f viewMatrix = this.transform.getViewMatrix();
        final float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        final float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        final float m32 = viewMatrix.m32();
        viewMatrix.m32(0);

        final Mesh mesh = skybox.getMesh();
        final Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(skybox, viewMatrix);
        this.skyboxShader.setUniform("viewModelMatrix", viewModelMatrix);
        this.skyboxShader.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
        this.skyboxShader.setUniform("colour", mesh.getMaterial().getAmbientColour());
        this.skyboxShader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);
        mesh.render();

        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
        this.skyboxShader.unbind();
    }

    public void renderScene(final Scene scene) {
        this.sceneShader.bind();

        this.sceneShader.setUniform("projectionMatrix", this.transform.getProjectionMatrix());
        this.sceneShader.setUniform("orthoProjectionMatrix", this.transform.getOrthoProjectionMatrix());
        final Matrix4f lightViewMatrix = this.transform.getLightViewMatrix();
        final Matrix4f viewMatrix = this.transform.getViewMatrix();

        final SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        this.sceneShader.setUniform("fog", scene.getFog());
        this.sceneShader.setUniform("textureSampler", 0);
        this.sceneShader.setUniform("normalMap", 1);
        this.sceneShader.setUniform("shadowMap", 2);
        this.sceneShader.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene, this.sceneShader, viewMatrix, lightViewMatrix);
        renderInstancedMeshes(scene, this.sceneShader, viewMatrix, lightViewMatrix);
        this.sceneShader.unbind();
    }

    private void renderNonInstancedMeshes(final Scene scene, final Shader shader, final Matrix4f viewMatrix, final Matrix4f lightViewMatrix) {
        this.sceneShader.setUniform("isInstanced", 0);
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            if (viewMatrix != null) {
                shader.setUniform("material", entry.getKey().getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, this.shadowMap.getDepthMapTexture().getId());
            }

            final Texture texture = entry.getKey().getMaterial().getTexture();
            if (texture != null) {
                this.sceneShader.setUniform("cols", texture.getCols());
                this.sceneShader.setUniform("rows", texture.getRows());
            }

            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        final Matrix4f modelMatrix = this.transform.buildModelMatrix(sceneElement);
                        if (viewMatrix != null) {
                            final Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(modelMatrix, viewMatrix);
                            this.sceneShader.setUniform("viewModelNonInstancedMatrix", viewModelMatrix);
                        }
                        final Matrix4f modelLightViewMatrix = this.transform.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                        this.sceneShader.setUniform("modelLightViewNonInstancedMatrix", modelLightViewMatrix);
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            shader.setUniform("jointsMatrix", animatedSceneElement.getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final Scene scene, final Shader shader, final Matrix4f viewMatrix, final Matrix4f lightViewMatrix) {
        shader.setUniform("isInstanced", 1);

        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : scene.getInstancedMeshSceneElements().entrySet()) {
            final Texture texture = entry.getKey().getMaterial().getTexture();
            if (texture != null) {
                this.sceneShader.setUniform("cols", texture.getCols());
                this.sceneShader.setUniform("rows", texture.getRows());
            }

            if (viewMatrix != null) {
                shader.setUniform("material", entry.getKey().getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, this.shadowMap.getDepthMapTexture().getId());
            }
            entry.getKey().renderListInstanced(entry.getValue(), this.transform, viewMatrix, lightViewMatrix);
        }
    }

    private void renderLights(final Matrix4f viewMatrix, final SceneLight sceneLight) {
        this.sceneShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        this.sceneShader.setUniform("specularPower", this.specularPower);

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

    private void renderHud(final Window window, final IHud hud) {
        if (hud != null) {
            this.hudShader.bind();

            final Matrix4f ortho = this.transform.getOrtho2DProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
            for (final SceneElement sceneElement : hud.getSceneElements()) {
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
        if (this.skyboxShader != null) {
            this.skyboxShader.cleanup();
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
