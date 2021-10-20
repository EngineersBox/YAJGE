package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.element.object.SceneObject;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.lighting.Attenuation;
import com.engineersbox.yajge.rendering.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.lighting.PointLight;
import com.engineersbox.yajge.rendering.lighting.SpotLight;
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

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private SceneObject[] sceneObjects;
    private Vector3f ambientLight;
    private PointLight[] pointLightList;
    private SpotLight[] spotLightList;
    private DirectionalLight directionalLight;
    private float lightAngle;
    private float spotAngle = 0;
    private float spotInc = 1;

    public TestGame() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.lightAngle = -90;
    }

    @Override
    public void init(final Window window) throws Exception {
        this.renderer.init(window);

        final float reflectance = 1f;

        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/cube.obj");
        final Texture texture = new Texture("assets/game/textures/grassblock.png");
        final Material material = new Material(texture, reflectance);

        mesh.setMaterial(material);
        final SceneObject sceneObject = new SceneObject(mesh);
        sceneObject.setScale(0.5f);
        sceneObject.setPosition(0, 0, -2);
        this.sceneObjects = new SceneObject[]{sceneObject};

        this.ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);

        // Point Light
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        Attenuation att = new Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        this.pointLightList = new PointLight[]{pointLight};

        // Spot Light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new Attenuation(0.0f, 0.0f, 0.02f);
        pointLight.setAttenuation(att);
        final Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        final SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        this.spotLightList = new SpotLight[]{spotLight, new SpotLight(spotLight)};

        lightPosition = new Vector3f(-1, 0, 0);
        this.directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
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
        final float lightPos = this.spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
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

        // Update spotlight direction
        this.spotAngle += this.spotInc * 0.05f;
        if (spotAngle > 2) {
            this.spotInc = -1;
        } else if (spotAngle < -2) {
            this.spotInc = 1;
        }
        final double spotAngleRad = Math.toRadians(this.spotAngle);
        final Vector3f coneDir = this.spotLightList[0].getConeDirection();
        coneDir.y = (float) Math.sin(spotAngleRad);

        // Update directional light direction, intensity and colour
        this.lightAngle += 1.1f;
        if (this.lightAngle > 90) {
            this.directionalLight.setIntensity(0);
            if (this.lightAngle >= 360) {
                this.lightAngle = -90;
            }
        } else if (this.lightAngle <= -80 || this.lightAngle >= 80) {
            final float factor = 1 - (Math.abs(this.lightAngle) - 80) / 10.0f;
            this.directionalLight.setIntensity(factor);
            this.directionalLight.getColor().y = Math.max(factor, 0.9f);
            this.directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            this.directionalLight.setIntensity(1);
            this.directionalLight.getColor().x = 1;
            this.directionalLight.getColor().y = 1;
            this.directionalLight.getColor().z = 1;
        }
        final double angRad = Math.toRadians(this.lightAngle);
        this.directionalLight.getDirection().x = (float) Math.sin(angRad);
        this.directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(final Window window) {
        this.renderer.render(
                window,
                this.camera,
                this.sceneObjects,
                this.ambientLight,
                this.pointLightList,
                this.spotLightList,
                this.directionalLight
        );
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (final SceneObject sceneObject : this.sceneObjects) {
            sceneObject.getMesh().cleanUp();
        }
    }
}
