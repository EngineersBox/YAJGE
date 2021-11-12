package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.IGameLogic;
import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.rendering.scene.lighting.Attenuation;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.loader.assimp.StaticMeshesLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Skybox;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.40f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private float angleInc;
    private float lightAngle;
    private float lightRotation;
    private float rotationInc;
    private boolean firstTime;
    private boolean sceneChanged;
    private final Map<String, SceneElement> elements = new HashMap<>();

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.angleInc = 0;
        this.lightAngle = 120f;
        this.firstTime = true;
    }

    @Override
    public void init(final Window window)  {
        this.renderer.init(window);
        this.scene = new Scene();

        final Mesh[] houseMesh = StaticMeshesLoader.load("assets/game/models/house/house.obj", "assets/game/models/house");
//        for (final Mesh mesh : houseMesh) {
//            mesh.setBoundingRadius(1.6f);
//        }
        final SceneElement house = new SceneElement(houseMesh);
        house.setRotation(new Quaternionf().rotateY(-5.5f));
//        house.setFrustumCulling(true);
        this.elements.put("house", house);

        final Mesh[] terrainMesh = StaticMeshesLoader.load("assets/game/models/terrain/terrain.obj", "assets/game/models/terrain");
        final SceneElement terrain = new SceneElement(terrainMesh);
        terrain.setScale(100.0f);
        this.elements.put("terrain", terrain);

        this.scene.setSceneElements(new SceneElement[]{house, terrain});
        this.scene.setRenderShadows(true);

        final Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
        this.scene.setFog(new Fog(true, fogColour, 0.02f));

        final float skyboxScale = 100.0f;
        final Skybox skybox = new Skybox("assets/game/models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skybox.setScale(skyboxScale);
        this.scene.setSkybox(skybox);

        configureLights();

        this.camera.getPosition().x = -17.0f;
        this.camera.getPosition().y =  17.0f;
        this.camera.getPosition().z = -30.0f;
        this.camera.getRotation().x = 20.0f;
        this.camera.getRotation().y = 140.f;
    }

    private void configureLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 0.8f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);

        sceneLight.setPointLights(new PointLight[]{new PointLight(
                new Vector3f(0.6f, 0.3f, 0.2f),
                new Vector3f(0.0f, 25.0f, 0.0f),
                lightIntensity,
                new Attenuation(1, 0.0f, 0)
        )});
    }

    @Override
    public void input(final Window window,
                      final MouseInput mouseInput) {
        this.sceneChanged = false;
        this.cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            this.sceneChanged = true;
            this.cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            this.sceneChanged = true;
            this.cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            this.sceneChanged = true;
            this.cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            this.sceneChanged = true;
            this.cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
            this.sceneChanged = true;
            this.cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            this.sceneChanged = true;
            this.cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            this.sceneChanged = true;
            this.angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            this.sceneChanged = true;
            this.angleInc += 0.05f;
        } else {
            this.angleInc = 0;
        }
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            this.elements.get("house").getRotation().rotateLocalY(0.01f);
            this.sceneChanged = true;
//            this.rotationInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            this.elements.get("house").getRotation().rotateLocalY(-0.01f);
            this.sceneChanged = true;
//            this.rotationInc += 0.05f;
        } else {
            this.rotationInc = 0;
        }
    }

    @Override
    public void update(final float interval,
                       final MouseInput mouseInput,
                       final Window window) {
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotVec = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
            this.sceneChanged = true;
        }

        this.camera.movePosition(
                this.cameraInc.x * CAMERA_POS_STEP,
                this.cameraInc.y * CAMERA_POS_STEP,
                this.cameraInc.z * CAMERA_POS_STEP
        );

        this.lightAngle = Math.min(Math.max(this.lightAngle + this.angleInc, 0), 180);
        this.lightRotation = Math.min(Math.max(this.lightRotation + this.rotationInc, 0), 360);
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = (float) Math.toRadians(this.lightRotation);
        lightDirection.y = (float) Math.sin(Math.toRadians(this.lightAngle));
        lightDirection.z = (float) Math.cos(Math.toRadians(this.lightAngle));
        lightDirection.normalize();
        this.camera.updateViewMatrix();
    }

    @Override
    public void render(final Window window) {
        if (this.firstTime) {
            this.sceneChanged = true;
            this.firstTime = false;
        }
        this.renderer.render(
                window,
                this.camera,
                this.scene,
                this.sceneChanged
        );
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        this.scene.cleanup();
    }
}