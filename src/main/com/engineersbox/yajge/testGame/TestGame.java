package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.primitive.Mesh;

import com.engineersbox.yajge.rendering.resources.textures.Texture;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

public class TestGame implements EngineLogic {

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
        final float[] positions = new float[] {
                // V0
                -0.5f, 0.5f, 0.5f,
                // V1
                -0.5f, -0.5f, 0.5f,
                // V2
                0.5f, -0.5f, 0.5f,
                // V3
                0.5f, 0.5f, 0.5f,
                // V4
                -0.5f, 0.5f, -0.5f,
                // V5
                0.5f, 0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,

                // Top face
                // V8: V4 repeated
                -0.5f, 0.5f, -0.5f,
                // V9: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V10: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V11: V3 repeated
                0.5f, 0.5f, 0.5f,

                // Right face
                // V12: V3 repeated
                0.5f, 0.5f, 0.5f,
                // V13: V2 repeated
                0.5f, -0.5f, 0.5f,

                // Left face
                // V14: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V15: V1 repeated
                -0.5f, -0.5f, 0.5f,

                // Bottom face
                // V16: V6 repeated
                -0.5f, -0.5f, -0.5f,
                // V17: V7 repeated
                0.5f, -0.5f, -0.5f,
                // V18: V1 repeated
                -0.5f, -0.5f, 0.5f,
                // V19: V2 repeated
                0.5f, -0.5f, 0.5f,
        };
        final float[] texCoords = new float[]{
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                0.0f, 0.0f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,

                // Top face
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.0f, 1.0f,
                0.5f, 1.0f,

                // Right face
                0.0f, 0.0f,
                0.0f, 0.5f,

                // Left face
                0.5f, 0.0f,
                0.5f, 0.5f,

                // Bottom face
                0.5f, 0.0f,
                1.0f, 0.0f,
                0.5f, 0.5f,
                1.0f, 0.5f,
        };
        final int[] indices = new int[]{
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                8, 10, 11, 9, 8, 11,
                // Right face
                12, 13, 7, 5, 12, 7,
                // Left face
                14, 15, 6, 4, 14, 6,
                // Bottom face
                16, 18, 19, 17, 16, 19,
                // Back face
                4, 6, 7, 5, 4, 7,
        };
        final Texture texture = new Texture("src/main/resources/game/textures/grassblock.png");
        final Mesh mesh = new Mesh(positions, texCoords, indices, texture);
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
            final Vector3f itemPos = sceneObject.getPosition();
            final float posx = itemPos.x + displxInc * 0.01f;
            final float posy = itemPos.y + displyInc * 0.01f;
            final float posz = itemPos.z + displzInc * 0.01f;
            sceneObject.setPosition(posx, posy, posz);

            float scale = sceneObject.getScale();
            scale += scaleInc * 0.05f;
            if (scale < 0) {
                scale = 0;
            }
            sceneObject.setScale(scale);

            float rotation = sceneObject.getRotation().x + 1.5f;
            if ( rotation > 360 ) {
                rotation = 0;
            }
            sceneObject.setRotation(rotation, rotation, rotation);
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
