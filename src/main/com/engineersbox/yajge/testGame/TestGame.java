package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.primitive.Mesh;

import com.engineersbox.yajge.rendering.resources.textures.Texture;
import com.engineersbox.yajge.rendering.view.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements EngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.5f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private int displxInc = 0;
    private int displyInc = 0;
    private int displzInc = 0;
    private int scaleInc = 0;
    private final Renderer renderer;
    private SceneObject[] sceneObjects;
    private final Camera camera;
    private final Vector3f cameraInc;

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f();
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
        final SceneObject sceneObject1 = new SceneObject(mesh);
        sceneObject1.setScale(0.5f);
        sceneObject1.setPosition(0, 0, -2);
        final SceneObject sceneObject2 = new SceneObject(mesh);
        sceneObject2.setScale(0.5f);
        sceneObject2.setPosition(0.5f, 0.5f, -2);
        final SceneObject sceneObject3 = new SceneObject(mesh);
        sceneObject3.setScale(0.5f);
        sceneObject3.setPosition(0, 0, -2.5f);
        final SceneObject sceneObject4 = new SceneObject(mesh);
        sceneObject4.setScale(0.5f);
        sceneObject4.setPosition(0.5f, 0, -2.5f);
        this.sceneObjects = new SceneObject[]{
                sceneObject1,
                sceneObject2,
                sceneObject3,
                sceneObject4
        };
    }

    @Override
    public void input(final Window window,
                      final MouseInput mouseInput) {
        this.cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            this.cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            this.cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            this.cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            this.cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
            this.cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            this.cameraInc.y = 1;
        }
    }

    @Override
    public void update(final float interval,
                       final MouseInput mouseInput) {
        this.camera.movePosition(
                this.cameraInc.x * CAMERA_POS_STEP,
                this.cameraInc.y * CAMERA_POS_STEP,
                this.cameraInc.z * CAMERA_POS_STEP
        );
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotVec = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
        }
    }

    @Override
    public void render(final Window window) {
        this.renderer.render(window, this.camera, this.sceneObjects);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (final SceneObject sceneObject : this.sceneObjects) {
            sceneObject.getMesh().cleanUp();
        }
    }
}
