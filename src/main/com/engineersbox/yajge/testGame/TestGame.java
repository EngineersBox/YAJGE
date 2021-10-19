package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.lighting.Attenuation;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.resources.materials.Material;
import com.engineersbox.yajge.rendering.resources.materials.Texture;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.primitive.OBJLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements EngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private SceneObject[] sceneObjects;
    private Vector3f ambientLight;
    private PointLight pointLight;

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void init(Window window) throws Exception {
        this.renderer.init(window);

        final float reflectance = 1f;
        //Mesh mesh = OBJLoader.loadMesh("/models/bunny.obj");
        //Material material = new Material(new Vector3f(0.2f, 0.5f, 0.5f), reflectance);

        final Mesh mesh = OBJLoader.loadMesh("src/main/resources/game/models/cube.obj");
        final Texture texture = new Texture("src/main/resources/game/textures/grassblock.png");
        final Material material = new Material(texture, reflectance);

        mesh.setMaterial(material);
        final SceneObject sceneObject = new SceneObject(mesh);
        sceneObject.setScale(0.5f);
        sceneObject.setPosition(0, 0, -2);
        this.sceneObjects = new SceneObject[]{sceneObject};

        this.ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
        this.pointLight = new PointLight(
                new Vector3f(1, 1, 1),
                new Vector3f(0, 0, 1),
                1.0f
        );
        this.pointLight.setAttenuation(new Attenuation(0.0f, 0.0f, 1.0f));
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
        final float lightPos = pointLight.getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.pointLight.getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.pointLight.getPosition().z = lightPos - 0.1f;
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
        this.renderer.render(window, camera, sceneObjects, ambientLight, pointLight);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (final SceneObject sceneObject : this.sceneObjects) {
            sceneObject.getMesh().cleanUp();
        }
    }
}
