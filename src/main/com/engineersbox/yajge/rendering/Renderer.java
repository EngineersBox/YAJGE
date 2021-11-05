package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajge.rendering.scene.shadow.ShadowCascade;
import com.engineersbox.yajge.rendering.scene.shadow.ShadowRenderer;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.rendering.view.culling.FrustrumCullingFilter;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.assets.shader.Shader;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;

public class Renderer {

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
    
    private final Transform transform;
    private final ShadowRenderer shadowRenderer;
    private Shader sceneShader;
    private Shader skyboxShader;
    private Shader particlesShader;
    private final FrustrumCullingFilter frustrumCullingFilter;
    private final List<SceneElement> filteredSceneElements;
    private final float specularPower;

    public Renderer() {
        this.transform = new Transform();
        this.shadowRenderer = new ShadowRenderer();
        this.frustrumCullingFilter = new FrustrumCullingFilter();
        this.filteredSceneElements = new ArrayList<>();
        this.specularPower = 10f;
    }

    public void init(final Window window) {
        this.shadowRenderer.init(window);
        setupSkyboxShader();
        setupSceneShader();
        setupParticlesShader();
    }

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene,
                       final boolean sceneChanged) {
        clear();

        if (ConfigHandler.CONFIG.render.camera.frustrumCulling) {
            this.frustrumCullingFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            this.frustrumCullingFilter.filter(scene.getMeshSceneElements());
            this.frustrumCullingFilter.filter(scene.getInstancedMeshSceneElements());
        }

        if (scene.shadowsEnabled() && sceneChanged) {
            this.shadowRenderer.render(
                    window,
                    scene,
                    camera,
                    this.transform,
                    this
            );
        }

        glViewport(0, 0, window.getWidth(), window.getHeight());

        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
        renderSkyBox(window, camera, scene);
        renderParticles(window, camera, scene);

        renderCrossHair(window);
//        renderAxes(window, camera);
    }

    private void setupParticlesShader() {
        this.particlesShader = new Shader();
        this.particlesShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.vert"));
        this.particlesShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.frag"));
        this.particlesShader.link();

        Stream.of(
                "viewMatrix",
                "projectionMatrix",
                "textureSampler",
                "cols",
                "rows"
        ).forEach(this.particlesShader::createUniform);
    }

    private void setupSkyboxShader() {
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

        Stream.of(
                "viewMatrix",
                "projectionMatrix",
                "textureSampler",
                "normalMap",
                "specularPower",
                "ambientLight",
                "modelNonInstancedMatrix",
                "renderShadow",
                "jointsMatrix",
                "isInstanced",
                "cols",
                "rows",
                "selectedNonInstanced"
        ).forEach(this.sceneShader::createUniform);
        this.sceneShader.createMaterialUniform("material");
        this.sceneShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        this.sceneShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
        this.sceneShader.createFogUniform("fog");

        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            this.sceneShader.createUniform("shadowMap_" + i);
        }
        this.sceneShader.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        this.sceneShader.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        this.sceneShader.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private void renderParticles(final Window window,
                                 final Camera camera,
                                 final Scene scene) {
        this.particlesShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.particlesShader.setUniform("viewMatrix", viewMatrix);
        this.particlesShader.setUniform("textureSampler", 0);
        this.particlesShader.setUniform("projectionMatrix", window.getProjectionMatrix());

        final IParticleEmitter[] emitters = scene.getParticleEmitters();
        if (emitters == null) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDepthMask(true);
            this.particlesShader.unbind();
            return;
        }

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (final IParticleEmitter emitter : emitters) {
            final InstancedMesh mesh = (InstancedMesh) emitter.getBaseParticle().getMesh();
            final Texture texture = mesh.getMaterial().getTexture();
            this.particlesShader.setUniform("cols", texture.getCols());
            this.particlesShader.setUniform("rows", texture.getRows());
            mesh.renderListInstanced(
                    emitter.getParticles(),
                    true,
                    this.transform,
                    viewMatrix
            );
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        this.particlesShader.unbind();
    }

    private void renderSkyBox(final Window window,
                              final Camera camera,
                              final Scene scene) {
        final Skybox skybox = scene.getSkybox();
        if (skybox == null) {
            return;
        }
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("textureSampler", 0);
        this.skyboxShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        final Matrix4f viewMatrix = camera.getViewMatrix();
        final float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        final float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        final float m32 = viewMatrix.m32();
        viewMatrix.m32(0);

        final Mesh mesh = skybox.getMesh();
        final Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(skybox, viewMatrix);
        this.skyboxShader.setUniform("viewModelMatrix", viewModelMatrix);
        this.skyboxShader.setUniform("ambientLight", scene.getSceneLight().getSkyboxLight());
        this.skyboxShader.setUniform("colour", mesh.getMaterial().getAmbientColour());
        this.skyboxShader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);
        mesh.render();

        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
        this.skyboxShader.unbind();
    }

    public void renderScene(final Window window,
                            final Camera camera,
                            final Scene scene) {
        this.sceneShader.bind();
        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.sceneShader.setUniform("viewMatrix", viewMatrix);
        this.sceneShader.setUniform("projectionMatrix", window.getProjectionMatrix());

        final List<ShadowCascade> shadowCascades = this.shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = shadowCascades.get(i);
            this.sceneShader.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            this.sceneShader.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            this.sceneShader.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }

        final SceneLight sceneLight = scene.getSceneLight();
        renderLights(viewMatrix, sceneLight);

        this.sceneShader.setUniform("fog", scene.getFog());
        this.sceneShader.setUniform("textureSampler", 0);
        this.sceneShader.setUniform("normalMap", 1);
        final int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            this.sceneShader.setUniform("shadowMap_" + i, start + i);
        }
        this.sceneShader.setUniform("renderShadow", scene.shadowsEnabled() ? 1 : 0);

        renderNonInstancedMeshes(scene);
        renderInstancedMeshes(scene, viewMatrix);
        this.sceneShader.unbind();
    }

    private void renderNonInstancedMeshes(final Scene scene) {
        this.sceneShader.setUniform("isInstanced", 0);
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            this.sceneShader.setUniform("material", entry.getKey().getMaterial());
            final Texture text = entry.getKey().getMaterial().getTexture();
            if (text != null) {
                this.sceneShader.setUniform("cols", text.getCols());
                this.sceneShader.setUniform("rows", text.getRows());
            }
            this.shadowRenderer.bindTextures(GL_TEXTURE2);

            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        this.sceneShader.setUniform("selectedNonInstanced", sceneElement.isSelected() ? 1.0f : 0.0f);
                        this.sceneShader.setUniform("modelNonInstancedMatrix", this.transform.buildModelMatrix(sceneElement));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            this.sceneShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final Scene scene, final Matrix4f viewMatrix) {
        this.sceneShader.setUniform("isInstanced", 1);

        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : scene.getInstancedMeshSceneElements().entrySet()) {
            final Texture texture = entry.getKey().getMaterial().getTexture();
            if (texture != null) {
                this.sceneShader.setUniform("cols", texture.getCols());
                this.sceneShader.setUniform("rows", texture.getRows());
            }
            this.sceneShader.setUniform("material", entry.getKey().getMaterial());
            this.filteredSceneElements.clear();
            entry.getValue()
                    .stream()
                    .filter(SceneElement::isInsideFrustum)
                    .forEach(this.filteredSceneElements::add);
            this.shadowRenderer.bindTextures(GL_TEXTURE2);
            entry.getKey().renderListInstanced(this.filteredSceneElements, this.transform, viewMatrix);
        }
    }

    private void renderLights(final Matrix4f viewMatrix,
                              final SceneLight sceneLight) {
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

    private void renderCrossHair(final Window window) {
        if (!window.getOptions().compatProfile()) {
            return;
        }
        glPushMatrix();
        glLoadIdentity();

        glLineWidth(2.0f);
        glBegin(GL_LINES);

        glColor3f(1.0f, 1.0f, 1.0f);

        // Horizontal line
        glVertex3f(-0.009f, 0.0f, 0.0f);
        glVertex3f(+0.01f, 0.0f, 0.0f);
        glEnd();

        // Vertical line
        glBegin(GL_LINES);
        glVertex3f(0.0f, -0.017f, 0.0f);
        glVertex3f(0.0f, +0.017f, 0.0f);
        glEnd();

        glPopMatrix();
    }

    private void renderAxes(final Window window,
                            final Camera camera) {
        if (!window.getOptions().compatProfile()) {
            return;
        }
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
        glVertex3f(0.02f, 0.0f, 0.0f);
        // Y Axis
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.03f, 0.0f);
        // Z Axis
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.02f);
        glEnd();

        glPopMatrix();
    }

    public void cleanup() {
        if (this.shadowRenderer != null) {
            this.shadowRenderer.cleanup();
        }
        if (this.skyboxShader != null) {
            this.skyboxShader.cleanup();
        }
        if (this.sceneShader != null) {
            this.sceneShader.cleanup();
        }
        if (this.particlesShader != null) {
            this.particlesShader.cleanup();
        }
    }
}
