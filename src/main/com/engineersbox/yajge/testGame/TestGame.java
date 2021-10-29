package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.IGameLogic;
import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.rendering.Renderer;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.loader.OBJLoader;
import com.engineersbox.yajge.scene.Scene;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.Terrain;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.FlowParticleEmitter;
import com.engineersbox.yajge.scene.element.particles.Particle;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private Terrain terrain;
    private float angleInc;
    private float lightAngle;
    private FlowParticleEmitter particleEmitter;
    
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
        final Mesh quadMesh = OBJLoader.loadMesh("assets/game/models/plane.obj");
        final Material quadMaterial = new Material(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f), reflectance);
        quadMesh.setMaterial(quadMaterial);
        final SceneElement quadSceneElement = new SceneElement(quadMesh);
        quadSceneElement.setPosition(0, 0, 0);
        quadSceneElement.setScale(2.5f);

        this.scene.getSceneElements(new SceneElement[] {quadSceneElement} );

        final Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        final long ttl = 4000;
        final int maxParticles = 200;
        final long creationPeriodMillis = 300;
        final float range = 0.2f;
        final float scale = 1.0f;
        final Mesh partMesh = OBJLoader.loadMesh("assets/game/models/particle.obj");
        final Texture texture = new Texture("assets/game/textures/particle_anim.png", 4, 4);
        final Material partMaterial = new Material(texture, reflectance);
        partMesh.setMaterial(partMaterial);
        final Particle particle = new Particle(partMesh, particleSpeed, ttl, 100);
        particle.setScale(scale);
        this.particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        this.particleEmitter.setActive(true);
        this.particleEmitter.setPositionRndRange(range);
        this.particleEmitter.setSpeedRndRange(range);
        this.particleEmitter.setAnimRange(10);
        this.scene.setParticleEmitters(new FlowParticleEmitter[] {this.particleEmitter});
        
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
    public void update(final float interval,
                       final MouseInput mouseInput) {
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

        this.lightAngle = Math.min(Math.max(this.lightAngle + this.angleInc, 0), 180);
        final float zValue = (float) Math.cos(Math.toRadians(this.lightAngle));
        final float yValue = (float) Math.sin(Math.toRadians(this.lightAngle));
        final Vector3f lightDirection = this.scene.getSceneLight()
                .getDirectionalLight()
                .getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        this.particleEmitter.update((long) (interval * 1000));
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
