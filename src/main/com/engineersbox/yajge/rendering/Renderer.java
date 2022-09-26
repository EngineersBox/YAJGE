package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.scene.SceneBuffer;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.shadow.ShadowCascade;
import com.engineersbox.yajge.rendering.scene.shadow.ShadowRenderer;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.rendering.view.GBuffer;
import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.rendering.view.culling.FrustumCullingFilter;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.assets.shader.Shader;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.resources.loader.assimp.StaticMeshesLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private static final Logger LOGGER = LogManager.getLogger(Renderer.class);

    public static final String SKYBOX_SHADER_NAME = "@yajge__internal__SKYBOX";
    public static final String PARTICLES_SHADER_NAME = "@yajge__internal__PARTICLES";
    public static final String GEOMETRY_SHADER_NAME = "@yajge__internal__GEOMETRY";
    public static final String DIRECTIONAL_LIGHT_SHADER_NAME = "@yajge__internal__DIRECTIONAL_LIGHT";
    public static final String POINT_LIGHT_SHADER_NAME = "@yajge__internal__POINT_LIGHT";
    public static final String FOG_SHADER_NAME = "@yajge__internal__FOG";

    private final Transform transform;
    private final ShadowRenderer shadowRenderer;
    private final Map<String, Shader> preProcessShaders;
    private final Map<String, TriConsumer<Window,Camera, Scene>> preProcessRenderHandlers;
    private final Map<String, Shader> lightingShaders;
    private final Map<String, TriConsumer<Window,Camera, Scene>> lightingRenderHandlers;
    private final Map<String, Shader> postProcessShaders;
    private final Map<String, TriConsumer<Window,Camera, Scene>> postProcessRenderHandlers;
    private final float specularPower;
    private final FrustumCullingFilter frustumFilter;
    private final List<SceneElement> filteredElements;
    private GBuffer gBuffer;
    private SceneBuffer sceneBuffer;
    private Mesh bufferPassMesh;
    private Matrix4f bufferPassModelMatrix;
    private final Vector4f tmpVec;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10f;
        this.shadowRenderer = new ShadowRenderer();
        this.frustumFilter = new FrustumCullingFilter();
        this.filteredElements = new ArrayList<>();
        this.preProcessShaders = new ListOrderedMap<>();
        this.lightingShaders = new ListOrderedMap<>();
        this.postProcessShaders = new ListOrderedMap<>();
        this.preProcessRenderHandlers = new HashMap<>();
        this.lightingRenderHandlers = new HashMap<>();
        this.postProcessRenderHandlers = new HashMap<>();
        this.tmpVec = new Vector4f();
    }

    public void init(final Window window) {
        this.shadowRenderer.init(window);
        this.gBuffer = new GBuffer(window);
        this.sceneBuffer = new SceneBuffer(window);

        LOGGER.debug("Configuring pre-process shaders");
        this.preProcessShaders.put(GEOMETRY_SHADER_NAME, configureGeometryShader());

        LOGGER.debug("Adding pre-process render handlers");
        this.preProcessRenderHandlers.put(GEOMETRY_SHADER_NAME, this::renderGeometry);

        LOGGER.debug("Configuring lighting shaders");
        this.lightingShaders.put(POINT_LIGHT_SHADER_NAME, configurePointLightShader());
        this.lightingShaders.put(DIRECTIONAL_LIGHT_SHADER_NAME, configureDirectionalLightShader());

        LOGGER.debug("Adding lighting render handlers");
        this.lightingRenderHandlers.put(POINT_LIGHT_SHADER_NAME, this::renderPointLights);
        this.lightingRenderHandlers.put(DIRECTIONAL_LIGHT_SHADER_NAME, this::renderDirectionalLight);

        LOGGER.debug("Configuring post-process shaders");
        this.postProcessShaders.put(FOG_SHADER_NAME, configureFogShader());
        this.postProcessShaders.put(SKYBOX_SHADER_NAME, configureSkyboxShader());
        this.postProcessShaders.put(PARTICLES_SHADER_NAME, configureParticlesShader());

        LOGGER.debug("Adding post-process render handlers");
        this.postProcessRenderHandlers.put(FOG_SHADER_NAME, this::renderFog);
        this.postProcessRenderHandlers.put(SKYBOX_SHADER_NAME, this::renderSkybox);
        this.postProcessRenderHandlers.put(PARTICLES_SHADER_NAME, this::renderParticles);

        this.bufferPassModelMatrix =  new Matrix4f();
        this.bufferPassMesh = StaticMeshesLoader.load("assets/game/models/buffer_pass_mess.obj", "models")[0];
    }

    public void registerPreProcessShaders(final ListOrderedMap<String, Shader> customPreProcessShaders,
                                          final Map<String, TriConsumer<Window,Camera, Scene>> customPreProcessRenderHandlers) {
        LOGGER.debug("Registering custom pre-process shaders");
        if (customPreProcessShaders != null) {
            this.preProcessShaders.putAll(customPreProcessShaders);
        }
        LOGGER.debug("Registering custom pre-process render handlers");
        if (customPreProcessRenderHandlers != null) {
            this.preProcessRenderHandlers.putAll(customPreProcessRenderHandlers);
        }
    }

    public void registerLightingShaders(final ListOrderedMap<String, Shader> customLightingShaders,
                                        final Map<String, TriConsumer<Window,Camera, Scene>> customLightingRenderHandlers) {
        LOGGER.debug("Registering custom lighting shaders");
        if (customLightingShaders != null) {
            this.lightingShaders.putAll(customLightingShaders);
        }
        LOGGER.debug("Registering custom lighting render handlers");
        if (customLightingRenderHandlers != null) {
            this.lightingRenderHandlers.putAll(customLightingRenderHandlers);
        }
    }

    public void registerPostProcessShaders(final ListOrderedMap<String, Shader> customPostProcessShaders,
                                           final Map<String, TriConsumer<Window,Camera, Scene>> customPostProcessRenderHandlers) {
        LOGGER.debug("Registering custom post-process shaders");
        if (customPostProcessShaders != null) {
            this.postProcessShaders.putAll(customPostProcessShaders);
        }
        LOGGER.debug("Registering custom post-process render handlers");
        if (customPostProcessRenderHandlers != null) {
            this.postProcessRenderHandlers.putAll(customPostProcessRenderHandlers);
        }
    }

    public void update(final Window window) {
        this.gBuffer.update(window);
        this.sceneBuffer.update(window);
    }

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene,
                       final boolean sceneChanged) {
        clear();

        if (window.getOptions().frustumCulling()) {
            this.frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            this.frustumFilter.filter(scene.getNonInstancedMeshes());
            this.frustumFilter.filter(scene.getInstancedMeshes());
        }

        if (scene.isRenderShadows() && sceneChanged) {
            this.shadowRenderer.render(window, scene, camera, this.transform, this);
        }

        glViewport(0, 0, window.getWidth(), window.getHeight());
        window.updateProjectionMatrix();
        for (final String preProcessShader : this.preProcessShaders.keySet()) {
            LOGGER.trace("[PRE-PROCESS] Running shader: {}", preProcessShader);
            this.preProcessRenderHandlers.get(preProcessShader).accept(window, camera, scene);
        }

        startLightRendering();
        for (final String lightingShader : this.lightingShaders.keySet()) {
            LOGGER.trace("[LIGHTING] Running shader: {}", lightingShader);
            this.lightingRenderHandlers.get(lightingShader).accept(window, camera, scene);
        }
        endLightRendering();

        for (final String postProcessShader : this.postProcessShaders.keySet()) {
            LOGGER.trace("[POST-PROCESS] Running shader: {}", postProcessShader);
            this.postProcessRenderHandlers.get(postProcessShader).accept(window, camera, scene);
        }
    }

    private Shader configureParticlesShader() {
        final Shader particlesShader = new Shader();
        particlesShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.vert"));
        particlesShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/particles/particles.frag"));
        particlesShader.link();
        Stream.of(
                "viewMatrix",
                "projectionMatrix",
                "textureSampler",
                "cols",
                "rows"
        ).forEach(particlesShader::createUniform);
        return particlesShader;
    }

    private Shader configureSkyboxShader() {
        final Shader skyboxShader = new Shader();
        skyboxShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.vert"));
        skyboxShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/skybox/skybox.frag"));
        skyboxShader.link();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "ambientLight",
                "colour",
                "hasTexture",
                "depthsText",
                "screenSize"
        ).forEach(skyboxShader::createUniform);
        return skyboxShader;
    }

    private Shader configureGeometryShader() {
        final Shader gBufferShader = new Shader();
        gBufferShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/scene/gbuffer.vert"));
        gBufferShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/scene/gbuffer.frag"));
        gBufferShader.link();

        gBufferShader.createMaterialUniform("material");
        Stream.of(
                "projectionMatrix",
                "viewMatrix",
                "textureSampler",
                "normalMap",
                "isInstanced",
                "modelNonInstancedMatrix",
                "selectedNonInstanced",
                "jointsMatrix",
                "cols",
                "rows",
                "renderShadow"
        ).forEach(gBufferShader::createUniform);

        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShader.createUniform("shadowMap_" + i);
        }
        gBufferShader.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShader.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        gBufferShader.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
        return gBufferShader;
    }

    private Shader configureDirectionalLightShader() {
        final Shader directionalLightShader = new Shader();
        directionalLightShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        directionalLightShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/directionalLight.frag"));
        directionalLightShader.link();
        Stream.of(
                "modelMatrix",
                "projectionMatrix",
                "screenSize",
                "positionsText",
                "diffuseText",
                "specularText",
                "normalsText",
                "shadowText",
                "specularPower",
                "ambientLight"
        ).forEach(directionalLightShader::createUniform);
        directionalLightShader.createDirectionalLightUniform("directionalLight");
        return directionalLightShader;
    }

    private Shader configurePointLightShader() {
        final Shader pointLightShader = new Shader();
        pointLightShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        pointLightShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/pointLight.frag"));
        pointLightShader.link();
        Stream.of(
                "modelMatrix",
                "projectionMatrix",
                "screenSize",
                "positionsText",
                "diffuseText",
                "specularText",
                "normalsText",
                "shadowText",
                "specularPower"
        ).forEach(pointLightShader::createUniform);
        pointLightShader.createPointLightUniform("pointLight");
        return pointLightShader;
    }

    private Shader configureFogShader() {
        final Shader fogShader = new Shader();
        fogShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        fogShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/scene/fog.frag"));
        fogShader.link();

        Stream.of(
                "modelMatrix",
                "viewMatrix",
                "projectionMatrix",
                "screenSize",
                "positionsText",
                "depthText",
                "sceneText",
                "ambientLight",
                "lightColour",
                "lightIntensity"
        ).forEach(fogShader::createUniform);
        fogShader.createFogUniform("fog");
        return fogShader;
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private void renderGeometry(final Window window,
                                final Camera camera,
                                final Scene scene) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.gBuffer.getGBufferId());
        clear();
        glDisable(GL_BLEND);
        final Shader gBufferShader = this.preProcessShaders.get(GEOMETRY_SHADER_NAME);
        gBufferShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        final Matrix4f projectionMatrix = window.getProjectionMatrix();
        gBufferShader.setUniform("viewMatrix", viewMatrix);
        gBufferShader.setUniform("projectionMatrix", projectionMatrix);
        gBufferShader.setUniform("textureSampler", 0);
        gBufferShader.setUniform("normalMap", 1);

        final List<ShadowCascade> shadowCascades = this.shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = shadowCascades.get(i);
            gBufferShader.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            gBufferShader.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            gBufferShader.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
        this.shadowRenderer.bindTextures(GL_TEXTURE2);
        final int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            gBufferShader.setUniform("shadowMap_" + i, start + i);
        }
        gBufferShader.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene);
        renderInstancedMeshes(scene, viewMatrix);
        gBufferShader.unbind();
        glEnable(GL_BLEND);
    }

    private void startLightRendering() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.sceneBuffer.getBufferId());

        clear();
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.gBuffer.getGBufferId());
    }

    private void endLightRendering() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void renderPointLights(final Window window,
                                   final Camera camera,
                                   final Scene scene) {
        final Shader pointLightShader = this.lightingShaders.get(POINT_LIGHT_SHADER_NAME);
        pointLightShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        pointLightShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        pointLightShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        pointLightShader.setUniform("specularPower", this.specularPower);

        final int[] textureIds = this.gBuffer.getTextureIds();
        final int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        pointLightShader.setUniform("positionsText", 0);
        pointLightShader.setUniform("diffuseText", 1);
        pointLightShader.setUniform("specularText", 2);
        pointLightShader.setUniform("normalsText", 3);
        pointLightShader.setUniform("shadowText", 4);
        pointLightShader.setUniform("screenSize", (float) this.gBuffer.getWidth(), (float) this.gBuffer.getHeight());

        final SceneLight sceneLight = scene.getSceneLight();
        final PointLight[] pointLights = sceneLight.getPointLightList();
        if (pointLights == null) {
            pointLightShader.unbind();
            return;
        }
        for (final PointLight pointLight : pointLights) {
            final PointLight currPointLight = new PointLight(pointLight);
            final Vector3f lightPos = currPointLight.getPosition();
            this.tmpVec.set(lightPos, 1);
            this.tmpVec.mul(viewMatrix);
            lightPos.x = this.tmpVec.x;
            lightPos.y = this.tmpVec.y;
            lightPos.z = this.tmpVec.z;
            pointLightShader.setUniform("pointLight", currPointLight);
            this.bufferPassMesh.render();
        }
        pointLightShader.unbind();
    }

    private void renderDirectionalLight(final Window window,
                                        final Camera camera,
                                        final Scene scene) {
        final Shader directionalLightShader = this.lightingShaders.get(DIRECTIONAL_LIGHT_SHADER_NAME);
        directionalLightShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        directionalLightShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        directionalLightShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        directionalLightShader.setUniform("specularPower", this.specularPower);

        final int[] textureIds = this.gBuffer.getTextureIds();
        final int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i = 0; i < numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        directionalLightShader.setUniform("positionsText", 0);
        directionalLightShader.setUniform("diffuseText", 1);
        directionalLightShader.setUniform("specularText", 2);
        directionalLightShader.setUniform("normalsText", 3);
        directionalLightShader.setUniform("shadowText", 4);
        directionalLightShader.setUniform("screenSize", (float) this.gBuffer.getWidth(), (float) this.gBuffer.getHeight());

        final SceneLight sceneLight = scene.getSceneLight();
        directionalLightShader.setUniform("ambientLight", sceneLight.getAmbientLight());

        final DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        this.tmpVec.set(currDirLight.getDirection(), 0).mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(this.tmpVec.x, this.tmpVec.y, this.tmpVec.z));
        directionalLightShader.setUniform("directionalLight", currDirLight);

        this.bufferPassMesh.render();
        directionalLightShader.unbind();
    }

    private void renderFog(final Window window,
                           final Camera camera,
                           final Scene scene) {
        final Shader fogShader = this.postProcessShaders.get(FOG_SHADER_NAME);
        fogShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        final Matrix4f projectionMatrix = window.getProjectionMatrix();
        fogShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        fogShader.setUniform("viewMatrix", viewMatrix);
        fogShader.setUniform("projectionMatrix", projectionMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getPositionTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getDepthTexture());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, this.sceneBuffer.getTextureId());

        fogShader.setUniform("positionsText", 0);
        fogShader.setUniform("depthText", 1);
        fogShader.setUniform("sceneText", 2);
        fogShader.setUniform("screenSize", (float) window.getWidth(), (float)window.getHeight());
        fogShader.setUniform("fog", scene.getFog());
        final SceneLight sceneLight = scene.getSceneLight();
        fogShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        final DirectionalLight dirLight = sceneLight.getDirectionalLight();
        fogShader.setUniform("lightColour", dirLight.getColor());
        fogShader.setUniform("lightIntensity", dirLight.getIntensity());

        this.bufferPassMesh.render();
        fogShader.unbind();
    }

    private void renderParticles(final Window window,
                                 final Camera camera,
                                 final Scene scene) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        final Shader particlesShader = this.postProcessShaders.get(PARTICLES_SHADER_NAME);
        particlesShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        particlesShader.setUniform("viewMatrix", viewMatrix);
        particlesShader.setUniform("textureSampler", 0);
        particlesShader.setUniform("projectionMatrix", window.getProjectionMatrix());

        final IParticleEmitter[] emitters = scene.getParticleEmitters();
        if (emitters == null) {
            particlesShader.unbind();
            return;
        }

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        for (final IParticleEmitter emitter : emitters) {
            final InstancedMesh mesh = (InstancedMesh) emitter.getBaseParticle().getMesh();
            final Texture texture = mesh.getMaterial().getTexture();
            particlesShader.setUniform("cols", texture.getCols());
            particlesShader.setUniform("rows", texture.getRows());
            mesh.renderInstanced(
                    emitter.getParticles(),
                    true,
                    this.transform,
                    viewMatrix
            );
        }

        glDisable(GL_BLEND);
        glDepthMask(true);
        particlesShader.unbind();
    }

    private void renderSkybox(final Window window,
                              final Camera camera,
                              final Scene scene) {
        final Skybox skybox = scene.getSkybox();
        if (skybox == null) {
            return;
        }

        final Shader skyboxShader = this.postProcessShaders.get(SKYBOX_SHADER_NAME);
        skyboxShader.bind();
        skyboxShader.setUniform("textureSampler", 0);
        skyboxShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        final Matrix4f viewMatrix = camera.getViewMatrix();
        final float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        final float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        final float m32 = viewMatrix.m32();
        viewMatrix.m32(0);

        final Mesh mesh = skybox.getMesh();
        final Matrix4f viewModelMatrix = this.transform.buildViewModelMatrix(skybox, viewMatrix);
        skyboxShader.setUniform("viewModelMatrix", viewModelMatrix);
        skyboxShader.setUniform("ambientLight", scene.getSceneLight().getSkyboxLight());
        skyboxShader.setUniform("colour", mesh.getMaterial().getDiffuseColour());
        skyboxShader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getDepthTexture());
        skyboxShader.setUniform("screenSize", (float) window.getWidth(), (float) window.getHeight());
        skyboxShader.setUniform("depthsText", 1);

        mesh.render();
        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
        skyboxShader.unbind();
    }

    private void renderNonInstancedMeshes(final Scene scene) {
        final Shader gBufferShader = this.preProcessShaders.get(GEOMETRY_SHADER_NAME);
        gBufferShader.setUniform("isInstanced", 0);

        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getNonInstancedMeshes().entrySet()) {
            gBufferShader.setUniform("material", entry.getKey().getMaterial());

            final Texture text = entry.getKey().getMaterial().getTexture();
            if (text != null) {
                gBufferShader.setUniform("cols", text.getCols());
                gBufferShader.setUniform("rows", text.getRows());
            }

            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        gBufferShader.setUniform("selectedNonInstanced", sceneElement.isSelected() ? 1.0f : 0.0f);
                        gBufferShader.setUniform("modelNonInstancedMatrix", this.transform.buildModelMatrix(sceneElement));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            gBufferShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentAnimation().getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final Scene scene,
                                       final Matrix4f viewMatrix) {
        final Shader gBufferShader = this.preProcessShaders.get(GEOMETRY_SHADER_NAME);
        gBufferShader.setUniform("isInstanced", 1);
        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : scene.getInstancedMeshes().entrySet()) {
            final Texture text = entry.getKey().getMaterial().getTexture();
            if (text != null) {
                gBufferShader.setUniform("cols", text.getCols());
                gBufferShader.setUniform("rows", text.getRows());
            }

            gBufferShader.setUniform("material", entry.getKey().getMaterial());
            this.filteredElements.clear();
            for (final SceneElement sceneElement : entry.getValue()) {
                if (sceneElement.isInsideFrustum()) {
                    this.filteredElements.add(sceneElement);
                }
            }
            entry.getKey().renderInstanced(this.filteredElements, this.transform, viewMatrix);
        }
    }

    public void cleanup() {
        if (this.shadowRenderer != null) {
            this.shadowRenderer.cleanup();
        }
        this.preProcessShaders.values().forEach(Shader::cleanup);
        this.lightingShaders.values().forEach(Shader::cleanup);
        this.postProcessShaders.values().forEach(Shader::cleanup);
        if (this.gBuffer != null) {
            this.gBuffer.cleanUp();
        }
        if (this.bufferPassMesh != null) {
            this.bufferPassMesh.cleanUp();
        }
    }
}
