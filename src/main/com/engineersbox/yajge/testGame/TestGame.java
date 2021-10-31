package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.IEngineLogic;
import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.loader.OBJLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.Terrain;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements IEngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.10f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private Terrain terrain;
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

        final float blockScale = 0.5f;
        final float skyBoxScale = 50.0f;
        final float extension = 2.0f;

        final float startX = extension * (-skyBoxScale + blockScale);
        final float startY = -1.0f;
        final float startZ = extension * (skyBoxScale - blockScale);
        final float inc = blockScale * 2;

        float posx = startX;
        float posz = startZ;
        final int NUM_ROWS = (int) (extension * skyBoxScale * 2 / inc);
        final int NUM_COLS = (int) (extension * skyBoxScale * 2/ inc);
        final SceneElement[] sceneElements  = new SceneElement[(NUM_ROWS * NUM_COLS) + 1];

        final Mesh cubeMesh = OBJLoader.loadMesh("assets/game/models/cube.obj");
        final Texture cubeTexture = new Texture("assets/game/textures/grassblock_t.png");
        final Material cubeMaterial = new Material(cubeTexture, 1f);
        cubeMesh.setMaterial(cubeMaterial);
        final SceneElement cubeSceneElement = new SceneElement(cubeMesh);
        cubeSceneElement.setScale(blockScale * 2);
        cubeSceneElement.setPosition(0, 1, 0);
        sceneElements[NUM_ROWS * NUM_COLS] = cubeSceneElement;

        final float reflectance = 1f;
        final int instances = NUM_ROWS * NUM_COLS;
        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/cube.obj", instances);
        final Texture texture = new Texture("assets/game/textures/grassblock.png");
        final Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);

        for(int i = 0; i < NUM_ROWS; i++) {
            for(int j = 0; j < NUM_COLS; j++) {
                final SceneElement sceneElement = new SceneElement(mesh);
                sceneElement.setScale(blockScale);
                sceneElement.setPosition(
                        posx,
                        startY + (Math.random() > 0.9f ? blockScale * 2 : 0f),
                        posz
                );
                sceneElements[i*NUM_COLS + j] = sceneElement;
                posx += inc;
            }
            posx = startX;
            posz -= inc;
        }
        this.scene.setSceneElements(sceneElements);
        this.scene.setRenderShadows(true);

        final Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
        this.scene.setFog(new Fog(true, fogColour, 0.05f));
        final Skybox skyBox = new Skybox(
                "assets/game/models/skybox.obj",
                new Vector4f(0.65f, 0.65f, 0.65f, 1.0f)
        );
        skyBox.setScale(skyBoxScale);
        this.scene.setSkybox(skyBox);
        setupLights();

        this.camera.getPosition().x = 0.25f;
        this.camera.getPosition().y = 6.5f;
        this.camera.getPosition().z = 6.5f;
        this.camera.getRotation().x = 25;
        this.camera.getRotation().y = -1;

        this.hud = new Hud("DEMO");
    }

    private void setupLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 1.0f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(10);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(final Window window, final MouseInput mouseInput) {
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
    public void update(final float interval, final MouseInput mouseInput) {
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotVec = mouseInput.getDisplVec();
            this.camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        final Vector3f prevPos = new Vector3f(this.camera.getPosition());
        this.camera.movePosition(this.cameraInc.x * CAMERA_POS_STEP, this.cameraInc.y * CAMERA_POS_STEP, this.cameraInc.z * CAMERA_POS_STEP);
        final float height = this.terrain != null ? this.terrain.getHeight(this.camera.getPosition()) : -Float.MAX_VALUE;
        if (this.camera.getPosition().y <= height) {
            this.camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

        this.lightAngle += this.angleInc;
        if (this.lightAngle < 0) {
            this.lightAngle = 0;
        } else if (this.lightAngle > 180) {
            this.lightAngle = 180;
        }
        final float zValue = (float) Math.cos(Math.toRadians(this.lightAngle));
        final float yValue = (float) Math.sin(Math.toRadians(this.lightAngle));
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
    }

    @Override
    public void render(final Window window) {
        if (this.hud != null) {
            this.hud.updateSize(window);
        }
        this.renderer.render(window, this.camera, this.scene, this.hud);
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