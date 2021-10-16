package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.engine.core.IGameLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.primitive.Mesh;

import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

public class TestGame implements IGameLogic {

    private int displxInc = 0;
    private int displyInc = 0;
    private int displzInc = 0;
    private int scaleInc = 0;
    private final Renderer renderer;

    private SceneObject[] sceneObjects;

    public TestGame() {
        renderer = new Renderer();
    }

    @Override
    public void init(final Window window) throws Exception {
        renderer.init(window);
        // Create the Mesh
        float[] positions = new float[]{
                -0.5f,  0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
        };
        float[] colours = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,
        };
        Mesh mesh = new Mesh(positions, colours, indices);
        final SceneObject sceneObject = new SceneObject(mesh);
        sceneObject.setPosition(0, 0, -2);
        sceneObjects = new SceneObject[] { sceneObject };
    }

    @Override
    public void input(Window window) {
        displyInc = 0;
        displxInc = 0;
        displzInc = 0;
        scaleInc = 0;
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            displyInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            displyInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            displxInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            displxInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_A)) {
            displzInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_Q)) {
            displzInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_Z)) {
            scaleInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            scaleInc = 1;
        }
    }

    @Override
    public void update(float interval) {
        for (final SceneObject sceneObject : this.sceneObjects) {
            // Update position
            final Vector3f itemPos = sceneObject.getPosition();
            final float posx = itemPos.x + displxInc * 0.01f;
            final float posy = itemPos.y + displyInc * 0.01f;
            final float posz = itemPos.z + displzInc * 0.01f;
            sceneObject.setPosition(posx, posy, posz);

            // Update scale
            float scale = sceneObject.getScale();
            scale += scaleInc * 0.05f;
            if ( scale < 0 ) {
                scale = 0;
            }
            sceneObject.setScale(scale);

            // Update rotation angle
            float rotation = sceneObject.getRotation().z + 1.5f;
            if (rotation > 360) {
                rotation = 0;
            }
            sceneObject.setRotation(0, 0, rotation);
        }
    }

    @Override
    public void render(final Window window) {
        renderer.render(window, this.sceneObjects);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (final SceneObject sceneObject : this.sceneObjects) {
            sceneObject.getMesh().cleanUp();
        }
    }
}
