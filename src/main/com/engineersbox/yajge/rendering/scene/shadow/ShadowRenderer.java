package com.engineersbox.yajge.rendering.scene.shadow;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.resources.assets.shader.Shader;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {

    public static final int NUM_CASCADES = 3;
    public static final float[] CASCADE_SPLITS = new float[]{
            ((float) ConfigHandler.CONFIG.render.camera.zFar) / 20.0f,
            ((float) ConfigHandler.CONFIG.render.camera.zFar) / 10.0f,
            (float) ConfigHandler.CONFIG.render.camera.zFar
    };

    private Shader depthShader;
    private List<ShadowCascade> shadowCascades;
    private ShadowBuffer shadowBuffer;
    private final List<SceneElement> filteredItems;

    public ShadowRenderer() {
        this.filteredItems = new ArrayList<>();
    }

    public void init(final Window window) {
        this.shadowBuffer = new ShadowBuffer();
        this.shadowCascades = new ArrayList<>();

        setupDepthShader();

        float zNear = (float) ConfigHandler.CONFIG.render.camera.zNear;
        for (int i = 0; i < NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            this.shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
    }

    public List<ShadowCascade> getShadowCascades() {
        return this.shadowCascades;
    }

    public void bindTextures(final int start) {
        this.shadowBuffer.bindTextures(start);
    }

    private void setupDepthShader() {
        this.depthShader = new Shader();
        this.depthShader.createVertexShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.vert"));
        this.depthShader.createFragmentShader(ResourceLoader.loadAsString("assets/game/shaders/lighting/depth.frag"));
        this.depthShader.link();
        Stream.of(
                "isInstanced",
                "modelNonInstancedMatrix",
                "lightViewMatrix",
                "jointsMatrix",
                "orthoProjectionMatrix"
        ).forEach(this.depthShader::createUniform);
    }

    private void update(final Window window, final Matrix4f viewMatrix, final Scene scene) {
        final SceneLight sceneLight = scene.getSceneLight();
        final DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
        this.shadowCascades.forEach((final ShadowCascade shadowCascade) -> shadowCascade.update(window, viewMatrix, directionalLight));
    }

    public void render(final Window window,
                       final Scene scene,
                       final Camera camera,
                       final Transform transform,
                       final Renderer renderer) {
        update(window, camera.getViewMatrix(), scene);
        glBindFramebuffer(GL_FRAMEBUFFER, this.shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);
        this.depthShader.bind();

        for (int i = 0; i < NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = this.shadowCascades.get(i);
            this.depthShader.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix());
            this.depthShader.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix());

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.shadowBuffer.getDepthMapTexture().getIds()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);
            renderNonInstancedMeshes(scene, transform);
            renderInstancedMeshes(scene, transform);
        }

        this.depthShader.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderNonInstancedMeshes(final Scene scene,
                                          final Transform transform) {
        this.depthShader.setUniform("isInstanced", 0);
        for (final Map.Entry<Mesh, List<SceneElement>> entry : scene.getMeshSceneElements().entrySet()) {
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        final Matrix4f modelMatrix = transform.buildModelMatrix(sceneElement);
                        this.depthShader.setUniform("modelNonInstancedMatrix", modelMatrix);
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            this.depthShader.setUniform("jointsMatrix", animatedSceneElement.getCurrentFrame().getJointMatrices());
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final Scene scene,
                                       final Transform transform) {
        this.depthShader.setUniform("isInstanced", 1);
        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : scene.getInstancedMeshSceneElements().entrySet()) {
            this.filteredItems.clear();
            entry.getValue()
                    .stream()
                    .filter(SceneElement::isInsideFrustum)
                    .forEach(this.filteredItems::add);
            bindTextures(GL_TEXTURE2);
            entry.getKey().renderListInstanced(this.filteredItems, transform, null);
        }
    }

    public void cleanup() {
        if (this.shadowBuffer != null) {
            this.shadowBuffer.cleanup();
        }
        if (this.depthShader != null) {
            this.depthShader.cleanup();
        }
    }

}
