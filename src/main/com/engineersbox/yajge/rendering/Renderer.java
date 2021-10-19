package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.element.transform.Transform;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.resources.shader.Shader;
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
        this.shader.createVertexShader(ResourceLoader.loadAsString("game/shaders/vertex.vert"));
        this.shader.createFragmentShader(ResourceLoader.loadAsString("game/shaders/fragment.frag"));
        this.shader.link();
        this.shader.createUniform("projectionMatrix");
        this.shader.createUniform("viewModelMatrix");
        this.shader.createUniform("textureSampler");
        this.shader.createMaterialUniform("material");
        this.shader.createUniform("specularPower");
        this.shader.createUniform("ambientLight");
        this.shader.createPointLightUniform("pointLight");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Window window,
                       final Camera camera,
                       final SceneObject[] sceneObjects,
                       final Vector3f ambientLight,
                       final PointLight pointLight) {
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
        this.shader.setUniform("specularPower", this.specularPower);
        final PointLight currPointLight = new PointLight(pointLight);
        final Vector3f lightPos = currPointLight.getPosition();
        final Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        this.shader.setUniform("pointLight", currPointLight);
        this.shader.setUniform("textureSampler", 0);

        for (final SceneObject sceneObject : sceneObjects) {
            final Mesh mesh = sceneObject.getMesh();
            final Matrix4f viewModelMatrix = this.transform.getViewModelMatrix(sceneObject, viewMatrix);
            this.shader.setUniform("viewModelMatrix", viewModelMatrix);
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
