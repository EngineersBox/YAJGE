package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.lighting.SpotLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.assets.shader.Shader;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.ResourceLoader;
import com.engineersbox.yajge.scene.gui.IHud;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.scene.object.SceneElement;
import com.engineersbox.yajge.scene.transform.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transform transform;
    private Shader sceneShader;
    private Shader hudShader;
    private final float specularPower;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10f;
    }

    public void init(final Window window) {
        setupSceneShader();
        setupHudShader();
    }

    private void setupSceneShader() {
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
        this.sceneShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        this.sceneShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        this.sceneShader.createDirectionalLightUniform("directionalLight");
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

    public void render(final Window window,
                       final Camera camera,
                       final SceneElement[] sceneElements,
                       final SceneLight sceneLight,
                       final IHud hud) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        renderScene(window, camera, sceneElements, sceneLight);
        renderHud(window, hud);
    }

    public void renderScene(final Window window,
                            final Camera camera,
                            final SceneElement[] sceneElements,
                            final SceneLight sceneLight) {
        this.sceneShader.bind();
        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix(
                FOV,
                window.getWidth(),
                window.getHeight(),
                Z_NEAR,
                Z_FAR
        );
        this.sceneShader.setUniform("projectionMatrix", projectionMatrix);
        final Matrix4f viewMatrix = this.transform.getViewMatrix(camera);
        renderLights(viewMatrix, sceneLight);
        this.sceneShader.setUniform("textureSampler", 0);
        for (final SceneElement sceneElement : sceneElements) {
            final Mesh mesh = sceneElement.getMesh();
            final Matrix4f modelViewMatrix = this.transform.getViewModelMatrix(sceneElement, viewMatrix);
            this.sceneShader.setUniform("viewModelMatrix", modelViewMatrix);
            this.sceneShader.setUniform("material", mesh.getMaterial());
            mesh.render();
        }
        this.sceneShader.unbind();
    }

    private void renderLights(final Matrix4f viewMatrix,
                              final SceneLight sceneLight) {
        this.sceneShader.setUniform("ambientLight", sceneLight.getAmbientLight());
        this.sceneShader.setUniform("specularPower", this.specularPower);

        final PointLight[] pointLights = sceneLight.getPointLights();
        int numLights = pointLights != null ? pointLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            final PointLight currPointLight = new PointLight(pointLights[i]);
            final Vector3f lightPos = currPointLight.getPosition();
            final Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            this.sceneShader.setUniform("pointLights", currPointLight, i);
        }

        final SpotLight[] spotLights = sceneLight.getSpotLights();
        numLights = spotLights != null ? spotLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            final SpotLight currSpotLight = new SpotLight(spotLights[i]);
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

        final Matrix4f ortho = this.transform.getOrthoProjectionMatrix(
                0,
                window.getWidth(),
                window.getHeight(),
                0
        );
        for (final SceneElement sceneElement : hud.getSceneElements()) {
            final Mesh mesh = sceneElement.getMesh();
            final Matrix4f projModelMatrix = transform.getOrthoProjModelMatrix(sceneElement, ortho);
            this.hudShader.setUniform("projModelMatrix", projModelMatrix);
            this.hudShader.setUniform("colour", sceneElement.getMesh().getMaterial().getAmbientColour());
            this.hudShader.setUniform("hasTexture", sceneElement.getMesh().getMaterial().isTextured() ? 1 : 0);
            mesh.render();
        }
        this.hudShader.unbind();
    }

    public void cleanup() {
        if (this.sceneShader != null) {
            this.sceneShader.cleanup();
        }
        if (this.hudShader != null) {
            this.hudShader.cleanup();
        }
    }
}
