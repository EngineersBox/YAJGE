package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.rendering.assets.materials.Texture;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.primitive.OBJLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements EngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;
    private static final float BLOCK_SCALE = 0.5f;
    private static final float SKYBOX_SCALE = 10.0f;
    private static final float EXTENSION = 2.0f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private float lightAngle;

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.lightAngle = -90;
    }

    @Override
    public void init(final Window window) {
        this.renderer.init(window);

        this.scene = new Scene();

        final float reflectance = 1f;
        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/cube.obj");
        final Texture texture = new Texture("assets/game/textures/grassblock.png");
        final Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);

        final float startx = EXTENSION * (-SKYBOX_SCALE + BLOCK_SCALE);
        final  float startz = EXTENSION * (SKYBOX_SCALE - BLOCK_SCALE);
        final float starty = -1.0f;
        final float inc = BLOCK_SCALE * 2;

        float posx = startx;
        float posz = startz;
        float incy;
        final int rows = (int)(EXTENSION * SKYBOX_SCALE * 2 / inc);
        final int cols = (int)(EXTENSION * SKYBOX_SCALE * 2/ inc);
        final SceneElement[] sceneElements  = new SceneElement[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                final SceneElement sceneElement = new SceneElement(mesh);
                sceneElement.setScale(BLOCK_SCALE);
                incy = Math.random() > 0.9f ? BLOCK_SCALE * 2 : 0f;
                sceneElement.setPosition(posx, starty + incy, posz);
                sceneElements[i*cols + j] = sceneElement;
                posx += inc;
            }
            posx = startx;
            posz -= inc;
        }
        this.scene.setSceneElements(sceneElements);

        final Skybox skyBox = new Skybox("assets/game/models/skybox.obj", "assets/game/textures/skybox.png");
        skyBox.setScale(SKYBOX_SCALE);
        this.scene.setSkybox(skyBox);

        setupLights();

        this.hud = new Hud("DEMO");

        this.camera.getPosition().x = 0.65f;
        this.camera.getPosition().y = 1.15f;
        this.camera.getPosition().y = 4.34f;
    }

    private void setupLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));
        Vector3f lightPosition = new Vector3f(-1, 0, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, 1.0f));
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
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
            this.hud.rotateCompass(this.camera.getRotation().y);
        }
        this.camera.movePosition(
                this.cameraInc.x * CAMERA_POS_STEP,
                this.cameraInc.y * CAMERA_POS_STEP,
                this.cameraInc.z * CAMERA_POS_STEP
        );

        final SceneLight sceneLight = this.scene.getSceneLight();
        final DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        this.lightAngle += 1.1f;
        if (this.lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (this.lightAngle >= 360) {
                this.lightAngle = -90;
            }
            sceneLight.getAmbientLight().set(0.3f, 0.3f, 0.4f);
        } else if (this.lightAngle <= -80 || this.lightAngle >= 80) {
            final float factor = 1 - (Math.abs(this.lightAngle) - 80) / 10.0f;
            sceneLight.getAmbientLight().set(factor, factor, factor);
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            sceneLight.getAmbientLight().set(1, 1, 1);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        final double angRad = Math.toRadians(this.lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(final Window window) {
        this.hud.updateSize(window);
        this.renderer.render(window, this.camera, this.scene, this.hud);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        scene.getMeshSceneElements()
                .keySet()
                .forEach(Mesh::cleanUp);
        this.hud.cleanup();
    }
}
