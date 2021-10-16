package com.engineersbox.yajge.rendering;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.element.transform.Transform;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.shader.Shader;
import com.engineersbox.yajge.resources.ResourceLoader;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private final Transform transform;
    private Shader shader;

    public Renderer() {
        transform = new Transform();
    }

    public void init(final Window window) throws Exception {
        this.shader = new Shader();
        this.shader.createVertexShader(ResourceLoader.load("shaders/vertex.vert"));
        this.shader.createFragmentShader(ResourceLoader.load("shaders/fragment.frag"));
        this.shader.link();
        this.shader.createUniform("projectionMatrix");
        this.shader.createUniform("worldMatrix");
        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Window window,
                       final SceneObject[] sceneObjects) {
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
        for (final SceneObject sceneObject : sceneObjects) {
            final Matrix4f worldMatrix = this.transform.getWorldMatrix(
                    sceneObject.getPosition(),
                    sceneObject.getRotation(),
                    sceneObject.getScale()
            );
            this.shader.setUniform("worldMatrix", worldMatrix);
            sceneObject.getMesh().render();
        }

        this.shader.unbind();
    }

    public void cleanup() {
        if (this.shader != null) {
            this.shader.cleanup();
        }
    }
}
