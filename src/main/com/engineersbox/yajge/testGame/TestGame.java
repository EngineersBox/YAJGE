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
import com.engineersbox.yajge.scene.element.interaction.MouseAABBSelectionDetector;
import com.engineersbox.yajge.scene.element.object.composite.HeightMapMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.particles.FlowParticleEmitter;
import com.engineersbox.yajge.scene.element.particles.Particle;
import com.engineersbox.yajge.scene.lighting.SceneLight;
import com.engineersbox.yajge.sound.SoundBuffer;
import com.engineersbox.yajge.sound.SoundListener;
import com.engineersbox.yajge.sound.SoundManager;
import com.engineersbox.yajge.sound.SoundSource;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.openal.AL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class TestGame implements IEngineLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.10f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final SoundManager soundMgr;
    private final Camera camera;
    private Scene scene;
    private final Hud hud;
    private float angleInc;
    private float lightAngle;
    private FlowParticleEmitter particleEmitter;
    private MouseAABBSelectionDetector selectDetector;
    private boolean leftButtonPressed;
    private boolean firstTime;
    private boolean sceneChanged;
    private SceneElement[] sceneElements;

    private enum Sounds {
        FIRE
    }

    public TestGame() {
        this.renderer = new Renderer();
        this.hud = new Hud();
        this.soundMgr = new SoundManager();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.angleInc = 0;
        this.lightAngle = 90;
        this.firstTime = true;
    }

    @Override
    public void init(final Window window) {
        this.hud.init(window);
        this.renderer.init(window);
        this.soundMgr.init();
        this.leftButtonPressed = false;
        this.scene = new Scene();

        final float reflectance = 1f;
        final float blockScale = 0.5f;
        final float skyBoxScale = 100.0f;
        final float extension = 2.0f;
        final float startX = extension * (-skyBoxScale + blockScale);
        final float startZ = extension * (skyBoxScale - blockScale);
        final float startY = -1.0f;
        final float inc = blockScale * 2;
        float posX = startX;
        float posZ = startZ;
        float incY;

        this.selectDetector = new MouseAABBSelectionDetector();

        final ByteBuffer buf;
        final int width;
        final int height;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);

            buf = STBImage.stbi_load("assets/game/textures/heightmap.png", w, h, channels, 4);
            if (buf == null) {
                throw new RuntimeException("Image file not loaded: " + STBImage.stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }

        final int instances = height * width;
        final Mesh mesh = OBJLoader.loadMesh("assets/game/models/cube.obj", instances);
        mesh.setBoundingRadius(1.6f);
        final Texture texture = new Texture("assets/game/textures/terrain_textures.png", 2, 1);
        final Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        this.sceneElements = new SceneElement[instances];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final SceneElement sceneElement = new SceneElement(mesh);
                sceneElement.setScale(blockScale);
                final int rgb = HeightMapMesh.getRGB(i, j, width, buf);
                incY = rgb / 650250; // 10 * 255 * 255
                sceneElement.setPosition(posX, startY + incY, posZ);
                final int textPos = Math.random() > 0.5f ? 0 : 1;
                sceneElement.setTextPos(textPos);
                this.sceneElements[i * width + j] = sceneElement;

                posX += inc;
            }
            posX = startX;
            posZ -= inc;
        }
        this.scene.setSceneElements(this.sceneElements);

        final int maxParticles = 200;
        final Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        final long ttl = 4000;
        final long creationPeriodMillis = 300;
        final float range = 0.2f;
        final float scale = 1.0f;
        final Mesh partMesh = OBJLoader.loadMesh("assets/game/models/particle.obj", maxParticles);
        final Texture particleTexture = new Texture("assets/game/textures/particle_anim.png", 4, 4);
        final Material partMaterial = new Material(particleTexture, reflectance);
        partMesh.setMaterial(partMaterial);
        final Particle particle = new Particle(partMesh, particleSpeed, ttl, 100);
        particle.setScale(scale);
        this.particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        this.particleEmitter.setActive(true);
        this.particleEmitter.setPositionRndRange(range);
        this.particleEmitter.setSpeedRndRange(range);
        this.particleEmitter.setAnimRange(10);
        this.scene.setParticleEmitters(new FlowParticleEmitter[]{this.particleEmitter});
        this.scene.setRenderShadows(true);

        final Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
        this.scene.setFog(new Fog(true, fogColour, 0.02f));

        final Skybox skyBox = new Skybox("assets/game/models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skyBox.setScale(skyBoxScale);
        this.scene.setSkybox(skyBox);
        configureLights();

        this.camera.getPosition().x = 0.25f;
        this.camera.getPosition().y = 6.5f;
        this.camera.getPosition().z = 6.5f;
        this.camera.getRotation().x = 25;
        this.camera.getRotation().y = -1;

        STBImage.stbi_image_free(buf);
        this.soundMgr.init();
        this.soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        configureSounds();
    }

    private void configureSounds() {
        final SoundBuffer buffFire = new SoundBuffer("assets/game/sounds/fire.ogg");
        this.soundMgr.addSoundBuffer(buffFire);
        final SoundSource sourceFire = new SoundSource(true, false);
        final Vector3f pos = this.particleEmitter.getBaseParticle().getPosition();
        sourceFire.setPosition(pos);
        sourceFire.setBuffer(buffFire.getBufferId());
        this.soundMgr.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();

        this.soundMgr.setListener(new SoundListener(new Vector3f()));
    }

    private void configureLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 1.0f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);
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
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = (float) Math.sin(Math.toRadians(this.lightAngle));
        lightDirection.z = (float) Math.cos(Math.toRadians(this.lightAngle));
        lightDirection.normalize();

        this.particleEmitter.update((long) (interval * 1000));
        this.camera.updateViewMatrix();

        this.soundMgr.updateListenerPosition(this.camera);

        final boolean aux = mouseInput.isLeftButtonPressed();
        if (aux && !this.leftButtonPressed && this.selectDetector.selectSceneElement(this.sceneElements, window, mouseInput.getCurrentPos(), this.camera)) {
            this.hud.incCounter();
        }
        this.leftButtonPressed = aux;
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
//        this.hud.render(window);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        this.soundMgr.cleanup();
        this.scene.cleanup();
        if (this.hud != null) {
            this.hud.cleanup();
        }
    }
}