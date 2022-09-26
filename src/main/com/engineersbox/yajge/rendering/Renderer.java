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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private final Transform transform;
    private final ShadowRenderer shadowRenderer;
    private Shader skyboxShader;
    private Shader particlesShader;
    private Shader gBufferShader;
    private Shader directionalLightShader;
    private Shader pointLightShader;
    private Shader fogShader;
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
        this.tmpVec = new Vector4f();
    }

    public void init(final Window window) {
        this.shadowRenderer.init(window);
        this.gBuffer = new GBuffer(window);
        this.sceneBuffer = new SceneBuffer(window);

        configureSkyboxShader();
        configureParticlesShader();
        configureGeometryShader();
        configureDirectionalLightShader();
        configurePointLightShader();
        configureFogShader();

        this.bufferPassModelMatrix =  new Matrix4f();
        this.bufferPassMesh = StaticMeshesLoader.load("assets/game/models/buffer_pass_mess.obj", "models")[0];
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
        renderGeometry(window, camera, scene);

        startLightRendering();
        renderPointLights(window, camera, scene);
        renderDirectionalLight(window, camera, scene);
        endLightRendering();

        renderFog(window, camera, scene);
        renderSkybox(window, camera, scene);
        renderParticles(window, camera, scene);
    }

    private void configureParticlesShader() {
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

    private void configureSkyboxShader() {
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
                "hasTexture",
                "depthsText",
                "screenSize"
        ).forEach(this.skyboxShader::createUniform);
    }

    private void configureGeometryShader() {
        this.gBufferShader = new Shader();
        this.gBufferShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/scene/gbuffer.vert"));
        this.gBufferShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/scene/gbuffer.frag"));
        this.gBufferShader.link();

        this.gBufferShader.createMaterialUniform("material");
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
        ).forEach(this.gBufferShader::createUniform);

        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            this.gBufferShader.createUniform("shadowMap_" + i);
        }
        this.gBufferShader.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        this.gBufferShader.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        this.gBufferShader.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
    }

    private void configureDirectionalLightShader() {
        this.directionalLightShader = new Shader();
        this.directionalLightShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        this.directionalLightShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/directionalLight.frag"));
        this.directionalLightShader.link();
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
        ).forEach(this.directionalLightShader::createUniform);
        this.directionalLightShader.createDirectionalLightUniform("directionalLight");
    }

    private void configurePointLightShader() {
        this.pointLightShader = new Shader();
        this.pointLightShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        this.pointLightShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/pointLight.frag"));
        this.pointLightShader.link();
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
        ).forEach(this.pointLightShader::createUniform);
        this.pointLightShader.createPointLightUniform("pointLight");
    }

    private void configureFogShader() {
        this.fogShader = new Shader();
        this.fogShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/light.vert"));
        this.fogShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/scene/fog.frag"));
        this.fogShader.link();

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
        ).forEach(this.fogShader::createUniform);
        this.fogShader.createFogUniform("fog");
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
        this.gBufferShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        final Matrix4f projectionMatrix = window.getProjectionMatrix();
        this.gBufferShader.setUniform("viewMatrix", viewMatrix);
        this.gBufferShader.setUniform("projectionMatrix", projectionMatrix);
        this.gBufferShader.setUniform("textureSampler", 0);
        this.gBufferShader.setUniform("normalMap", 1);

        final List<ShadowCascade> shadowCascades = this.shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = shadowCascades.get(i);
            this.gBufferShader.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            this.gBufferShader.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            this.gBufferShader.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
        this.shadowRenderer.bindTextures(GL_TEXTURE2);
        final int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            this.gBufferShader.setUniform("shadowMap_" + i, start + i);
        }
        this.gBufferShader.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene);
        renderInstancedMeshes(scene, viewMatrix);
        this.gBufferShader.unbind();
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
        this.pointLightShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.pointLightShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        this.pointLightShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        this.pointLightShader.setUniform("specularPower", this.specularPower);

        final int[] textureIds = this.gBuffer.getTextureIds();
        final int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i=0; i<numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        this.pointLightShader.setUniform("positionsText", 0);
        this.pointLightShader.setUniform("diffuseText", 1);
        this.pointLightShader.setUniform("specularText", 2);
        this.pointLightShader.setUniform("normalsText", 3);
        this.pointLightShader.setUniform("shadowText", 4);
        this.pointLightShader.setUniform("screenSize", (float) this.gBuffer.getWidth(), (float) this.gBuffer.getHeight());

        final SceneLight sceneLight = scene.getSceneLight();
        final PointLight[] pointLights = sceneLight.getPointLightList();
        if (pointLights == null) {
            this.pointLightShader.unbind();
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
            this.pointLightShader.setUniform("pointLight", currPointLight);
            this.bufferPassMesh.render();
        }
        this.pointLightShader.unbind();
    }

    private void renderDirectionalLight(final Window window,
                                        final Camera camera,
                                        final Scene scene) {
        this.directionalLightShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.directionalLightShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        this.directionalLightShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        this.directionalLightShader.setUniform("specularPower", this.specularPower);

        final int[] textureIds = this.gBuffer.getTextureIds();
        final int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i = 0; i < numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        this.directionalLightShader.setUniform("positionsText", 0);
        this.directionalLightShader.setUniform("diffuseText", 1);
        this.directionalLightShader.setUniform("specularText", 2);
        this.directionalLightShader.setUniform("normalsText", 3);
        this.directionalLightShader.setUniform("shadowText", 4);
        this.directionalLightShader.setUniform("screenSize", (float) this.gBuffer.getWidth(), (float) this.gBuffer.getHeight());

        final SceneLight sceneLight = scene.getSceneLight();
        this.directionalLightShader.setUniform("ambientLight", sceneLight.getAmbientLight());

        final DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        this.tmpVec.set(currDirLight.getDirection(), 0).mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(this.tmpVec.x, this.tmpVec.y, this.tmpVec.z));
        this.directionalLightShader.setUniform("directionalLight", currDirLight);

        this.bufferPassMesh.render();
        this.directionalLightShader.unbind();
    }

    private void renderFog(final Window window,
                           final Camera camera,
                           final Scene scene) {
        this.fogShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        final Matrix4f projectionMatrix = window.getProjectionMatrix();
        this.fogShader.setUniform("modelMatrix", this.bufferPassModelMatrix);
        this.fogShader.setUniform("viewMatrix", viewMatrix);
        this.fogShader.setUniform("projectionMatrix", projectionMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getPositionTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getDepthTexture());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, this.sceneBuffer.getTextureId());

        this.fogShader.setUniform("positionsText", 0);
        this.fogShader.setUniform("depthText", 1);
        this.fogShader.setUniform("sceneText", 2);
        this.fogShader.setUniform("screenSize", (float) window.getWidth(), (float)window.getHeight());
        this.fogShader.setUniform("fog", scene.getFog());
        final SceneLight sceneLight = scene.getSceneLight();
        this.fogShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        final DirectionalLight dirLight = sceneLight.getDirectionalLight();
        this.fogShader.setUniform("lightColour", dirLight.getColor());
        this.fogShader.setUniform("lightIntensity", dirLight.getIntensity());

        this.bufferPassMesh.render();
        this.fogShader.unbind();
    }

    private void renderParticles(final Window window,
                                 final Camera camera,
                                 final Scene scene) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        this.particlesShader.bind();

        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.particlesShader.setUniform("viewMatrix", viewMatrix);
        this.particlesShader.setUniform("textureSampler", 0);
        this.particlesShader.setUniform("projectionMatrix", window.getProjectionMatrix());

        final IParticleEmitter[] emitters = scene.getParticleEmitters();
        if (emitters == null) {
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
            mesh.renderInstanced(
                    emitter.getParticles(),
                    true,
                    this.transform,
                    viewMatrix
            );
        }

        glDisable(GL_BLEND);
        glDepthMask(true);
        this.particlesShader.unbind();
    }

    private void renderSkybox(final Window window,
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
        this.skyboxShader.setUniform("colour", mesh.getMaterial().getDiffuseColour());
        this.skyboxShader.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.gBuffer.getDepthTexture());
        this.skyboxShader.setUniform("screenSize", (float) window.getWidth(), (float) window.getHeight());
        this.skyboxShader.setUniform("depthsText", 1);

        mesh.render();
        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
        this.skyboxShader.unbind();
    }

    private void renderNonInstancedMeshes(final Scene scene) {
        this.gBufferShader.setUniform("isInstanced", 0);

        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getNonInstancedMeshes().entrySet()) {
            this.gBufferShader.setUniform("material", entry.getKey().getMaterial());

            final Texture text = entry.getKey().getMaterial().getTexture();
            if (text != null) {
                this.gBufferShader.setUniform("cols", text.getCols());
                this.gBufferShader.setUniform("rows", text.getRows());
            }

            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        this.gBufferShader.setUniform("selectedNonInstanced", sceneElement.isSelected() ? 1.0f : 0.0f);
                        this.gBufferShader.setUniform("modelNonInstancedMatrix", this.transform.buildModelMatrix(sceneElement));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            this.gBufferShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentAnimation().getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final Scene scene,
                                       final Matrix4f viewMatrix) {
        this.gBufferShader.setUniform("isInstanced", 1);
        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : scene.getInstancedMeshes().entrySet()) {
            final Texture text = entry.getKey().getMaterial().getTexture();
            if (text != null) {
                this.gBufferShader.setUniform("cols", text.getCols());
                this.gBufferShader.setUniform("rows", text.getRows());
            }

            this.gBufferShader.setUniform("material", entry.getKey().getMaterial());
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
        if (this.skyboxShader != null) {
            this.skyboxShader.cleanup();
        }
        if (this.particlesShader != null) {
            this.particlesShader.cleanup();
        }
        if (this.gBufferShader != null) {
            this.gBufferShader.cleanup();
        }
        if (this.directionalLightShader != null) {
            this.directionalLightShader.cleanup();
        }
        if (this.pointLightShader != null) {
            this.pointLightShader.cleanup();
        }
        if (this.gBuffer != null) {
            this.gBuffer.cleanUp();
        }
        if (this.bufferPassMesh != null) {
            this.bufferPassMesh.cleanUp();
        }
    }
}
