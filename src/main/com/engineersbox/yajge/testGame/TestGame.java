package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.Skybox;
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

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements EngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private static final float SKYBOX_SCALE = 10.0f;

    private static final float TERRAIN_SCALE = 10;
    private static final int TERRAIN_SIZE = 3;
    private static final float TERRAIN_MIN_Y = -0.1f;
    private static final float TERRAIN_MAX_Y = 0.1f;
    private static final int TERRAIN_TEX_INC = 40;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Terrain terrain;
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
        this.terrain = new Terrain(
                TERRAIN_SIZE,
                TERRAIN_SCALE,
                TERRAIN_MIN_Y,
                TERRAIN_MAX_Y,
                "assets/game/textures/heightmap.png",
                "assets/game/textures/terrain.png",
                TERRAIN_TEX_INC
        );
        this.scene.setSceneElements(terrain.getSceneElements());
        scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.15f));

        final Skybox skyBox = new Skybox("assets/game/models/skybox.obj", "assets/game/textures/skybox.png");
        skyBox.setScale(SKYBOX_SCALE);
        this.scene.setSkybox(skyBox);
        setupLights();

        this.hud = new Hud("DEMO");
        this.camera.getPosition().x = 0.0f;
        this.camera.getPosition().z = 0.0f;
        this.camera.getPosition().y = -0.2f;
        this.camera.getRotation().x = 10.f;
    }

    private void setupLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));
        final Vector3f lightPosition = new Vector3f(-1, 0, 0);
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

        final Vector3f prevPos = new Vector3f(this.camera.getPosition());
        this.camera.movePosition(
                this.cameraInc.x * CAMERA_POS_STEP,
                this.cameraInc.y * CAMERA_POS_STEP,
                this.cameraInc.z * CAMERA_POS_STEP
        );
        final float height = this.terrain.getHeight(this.camera.getPosition());
        if (this.camera.getPosition().y <= height)  {
            this.camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

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
