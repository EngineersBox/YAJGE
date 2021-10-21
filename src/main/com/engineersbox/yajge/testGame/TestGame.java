package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.rendering.lighting.Attenuation;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.scene.object.SceneElement;
import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
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
    private SceneElement[] sceneElements;
    private SceneLight sceneLight;
    private Hud hud;
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
    public void init(final Window window) {
        this.renderer.init(window);
        final float reflectance = 1f;

        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/cube.obj");
        final Texture texture = new Texture("assets/game/textures/grassblock.png");
        final Material material = new Material(texture, reflectance);

        mesh.setMaterial(material);
        final SceneElement sceneElement = new SceneElement(mesh);
        sceneElement.setScale(0.5f);
        sceneElement.setPosition(0, 0, -2);
        sceneElements = new SceneElement[]{sceneElement};

        this.sceneLight = new SceneLight();
        this.sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));

        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        Attenuation att = new Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        this.sceneLight.setPointLightList(new PointLight[]{pointLight});

        lightPosition = new Vector3f(0, 0.0f, 10f);
        pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new Attenuation(0.0f, 0.0f, 0.02f);
        pointLight.setAttenuation(att);
        final Vector3f coneDir = new Vector3f(0, 0, -1);
        final float cutoff = (float) Math.cos(Math.toRadians(140));
        final SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        this.sceneLight.setSpotLightList(new SpotLight[]{spotLight, new SpotLight(spotLight)});

        lightPosition = new Vector3f(-1, 0, 0);
        this.sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));

        hud = new Hud("DEMO");
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
        final SpotLight[] spotLightList = sceneLight.getSpotLightList();
        final float lightPos = spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
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
            Vector2f rotVec = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
            this.hud.rotateCompass(this.camera.getRotation().y);
        }

        this.spotAngle += this.spotInc * 0.05f;
        if (this.spotAngle > 2) {
            this.spotInc = -1;
        } else if (this.spotAngle < -2) {
            this.spotInc = 1;
        }
        final double spotAngleRad = Math.toRadians(this.spotAngle);
        final SpotLight[] spotLightList = this.sceneLight.getSpotLightList();
        final Vector3f coneDir = spotLightList[0].getConeDirection();
        coneDir.y = (float) Math.sin(spotAngleRad);

        // Update directional light direction, intensity and colour
        final DirectionalLight directionalLight = this.sceneLight.getDirectionalLight();
        this.lightAngle += 1.1f;
        if (this.lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (this.lightAngle >= 360) {
                this.lightAngle = -90;
            }
        } else if (this.lightAngle <= -80 || this.lightAngle >= 80) {
            final float factor = 1 - (Math.abs(this.lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        final double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(final Window window) {
        this.hud.updateSize(window);
        this.renderer.render(
                window,
                this.camera,
                this.sceneElements,
                this.sceneLight,
                this.hud
        );
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (final SceneElement sceneElement : this.sceneElements) {
            sceneElement.getMesh().cleanUp();
        }
        this.hud.cleanup();
    }
}
