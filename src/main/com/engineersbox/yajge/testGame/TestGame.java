package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.resources.primitive.OBJLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Terrain;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.object.composite.Mesh;
import com.engineersbox.yajge.rendering.view.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements EngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private Terrain terrain;
    private SceneElement giantCube;
    private float angleInc;
    private float lightAngle;

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.angleInc = 0;
        this.lightAngle = 45;
    }

    @Override
    public void init(final Window window) {
        this.renderer.init(window);
        this.scene = new Scene();

        final float reflectance = 1f;
        final Mesh cubeMesh = OBJLoader.loadMesh("assets/game/models/cube.obj");
        final Material cubeMaterial = new Material(new Vector4f(0, 1, 0, 1), reflectance);
        cubeMesh.setMaterial(cubeMaterial);
        this.giantCube = new SceneElement(cubeMesh);
        this.giantCube.setPosition(0, 0, 0);
        this.giantCube.setScale(0.5f);

        final Mesh quadMesh = OBJLoader.loadMesh("assets/game/models/plane.obj");
        final Material quadMaterial = new Material(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f), reflectance);
        quadMesh.setMaterial(quadMaterial);
        final SceneElement quad = new SceneElement(quadMesh);
        quad.setPosition(0, -1, 0);
        quad.setScale(2.5f);
        this.scene.setSceneElements(new SceneElement[]{this.giantCube, quad});

        setupLights();
        this.camera.getPosition().z = 2;
        this.hud = new Hud("Light Angle:");
    }

    private void setupLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 1.0f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMultiplier(5);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
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
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            this.angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            this.angleInc += 0.05f;
        } else {
            this.angleInc = 0;
        }
    }

    @Override
    public void update(final float interval,
                       final MouseInput mouseInput) {
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotVec = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
        }

        final Vector3f prevPos = new Vector3f(this.camera.getPosition());
        this.camera.movePosition(
                this.cameraInc.x * CAMERA_POS_STEP,
                this.cameraInc.y * CAMERA_POS_STEP,
                this.cameraInc.z * CAMERA_POS_STEP
        );

        final float height = this.terrain != null ? this.terrain.getHeight(this.camera.getPosition()) : -Float.MAX_VALUE;
        if (this.camera.getPosition().y <= height) {
            this.camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

        float rotY = this.giantCube.getRotation().y;
        rotY += 0.5f;
        if ( rotY >= 360 ) {
            rotY -= 360;
        }
        this.giantCube.getRotation().y = rotY;

        this.lightAngle = Math.max(Math.min(this.lightAngle + this.angleInc, 180), 0);
        final float zValue = (float)Math.cos(Math.toRadians(this.lightAngle));
        final float yValue = (float)Math.sin(Math.toRadians(this.lightAngle));
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        this.hud.setStatusText("LightAngle: " + (float) Math.toDegrees(Math.acos(lightDirection.z)));
    }

    @Override
    public void render(final Window window) {
        if (this.hud != null) {
            this.hud.updateSize(window);
        }
        this.renderer.render(
                window,
                this.camera,
                this.scene,
                this.hud
        );
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        this.scene.cleanup();
        if (this.hud != null) {
            this.hud.cleanup();
        }
    }
}
