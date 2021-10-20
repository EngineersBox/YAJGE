package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.element.transform.Transform;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.assets.shader.Shader;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.ResourceLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private final Transform transform;
    private Shader shader;
    private float specularPower;

    public Renderer() {
        this.transform = new Transform();
        this.specularPower = 10.0f;
    }

    public void init(final Window window) throws Exception {
        this.shader = new Shader();
        this.shader.createVertexShader(ResourceLoader.loadAsString("assets/game/lighting/shaders/final.vert"));
        this.shader.createFragmentShader(ResourceLoader.loadAsString("assets/game/lighting/shaders/final.frag"));
        this.shader.link();
        this.shader.createUniform("projectionMatrix");
        this.shader.createUniform("viewModelMatrix");
        this.shader.createUniform("textureSampler");
        this.shader.createMaterialUniforms("material");
        this.shader.createUniform("specularPower");
        this.shader.createUniform("ambientLight");
        this.shader.createPointLightUniforms("pointLight");
        this.shader.createDirectionalLightUniforms("directionalLight");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Window window,
                       final Camera camera,
                       final SceneObject[] sceneObjects,
                       final Vector3f ambientLight,
                       final PointLight pointLight,
                       final DirectionalLight directionalLight) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        this.shader.bind();
        final Matrix4f projectionMatrix = this.transform.getProjectionMatrix(
                FOV,
                window.getWidth(),
                window.getHeight(),
                Z_NEAR,
                Z_FAR
        );
        this.shader.setUniform("projectionMatrix", projectionMatrix);
        final Matrix4f viewMatrix = this.transform.getViewMatrix(camera);

        this.shader.setUniform("ambientLight", ambientLight);
        this.shader.setUniform("specularPower", specularPower);

        final PointLight currPointLight = new PointLight(pointLight);
        final Vector3f lightPos = currPointLight.getPosition();
        final Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        this.shader.setUniform("pointLight", currPointLight);

        final DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        final Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        this.shader.setUniform("directionalLight", currDirLight);

        this.shader.setUniform("textureSampler", 0);
        for (final SceneObject sceneObject : sceneObjects) {
            Mesh mesh = sceneObject.getMesh();
            Matrix4f modelViewMatrix = this.transform.getViewModelMatrix(sceneObject, viewMatrix);
            this.shader.setUniform("viewModelMatrix", modelViewMatrix);
            this.shader.setUniform("material", mesh.getMaterial());
            mesh.render();
        }

        this.shader.unbind();
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
        }
    }
}
