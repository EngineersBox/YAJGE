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
    private static final float CAMERA_POS_STEP_ACCELERATED = 0.30f;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final SoundManager soundManager;
    private final Camera camera;
    private Scene scene;
    private Hud hud;
    private Terrain terrain;
    private float angleInc;
    private float lightAngle;
    private FlowParticleEmitter particleEmitter;
    private float accelerationMultiplier = CAMERA_POS_STEP;

    private enum Sounds { MUSIC, FIRE }

    public TestGame() {
        this.renderer = new Renderer();
        this.soundManager = new SoundManager();
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
        final float blockScale = 0.5f;
        final float skyboxScale = 100.0f;
        final float extension = 2.0f;
        final float startx = extension * (-skyboxScale + blockScale);
        final float startz = extension * (skyboxScale - blockScale);
        final float starty = -1.0f;
        final float inc = blockScale * 2;
        float posx = startx;
        float posz = startz;
        float incy;

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
        final Texture texture = new Texture("assets/game/textures/terrain_textures.png", 2, 1);
        final Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        final SceneElement[] sceneElements = new SceneElement[instances];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final SceneElement gameItem = new SceneElement(mesh);
                gameItem.setScale(blockScale);
                final int rgb = HeightMapMesh.getRGB(i, j, width, buf);
                incy = rgb / (float) (10 * 255 * 255);
                gameItem.setPosition(posx, starty + incy, posz);
                final int textPos = Math.random() > 0.5f ? 0 : 1;
                gameItem.setTextPos(textPos);
                sceneElements[i * width + j] = gameItem;

                posx += inc;
            }
            posx = startx;
            posz -= inc;
        }
        this.scene.setSceneElements(sceneElements);
        STBImage.stbi_image_free(buf);

        final int maxParticles = 200;
        final Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        final long creationPeriodMillis = 300;
        final float range = 0.2f;
        final float scale = 1.0f;
        final Mesh partMesh = OBJLoader.loadMesh("assets/game/models/particle.obj", maxParticles);
        final Texture particleTexture = new Texture("assets/game/textures/particle_anim.png", 4, 4);
        final Material partMaterial = new Material(particleTexture, reflectance);
        partMesh.setMaterial(partMaterial);
        final Particle particle = new Particle(
                partMesh,
                particleSpeed,
                4000,
                100
        );
        particle.setScale(scale);
        this.particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        this.particleEmitter.setActive(true);
        this.particleEmitter.setPositionRndRange(range);
        this.particleEmitter.setSpeedRndRange(range);
        this.particleEmitter.setAnimRange(10);
        this.scene.setParticleEmitters(new FlowParticleEmitter[]{this.particleEmitter});

        this.scene.setRenderShadows(false);
        final Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
        this.scene.setFog(new Fog(true, fogColour, 0.02f));
        final Skybox skybox = new Skybox("assets/game/models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skybox.setScale(skyboxScale);
        this.scene.setSkybox(skybox);

        configureLights();
        this.camera.getPosition().x = 0.25f;
        this.camera.getPosition().y = 6.5f;
        this.camera.getPosition().z = 6.5f;
        this.camera.getRotation().x = 25;
        this.camera.getRotation().y = -1;

        this.hud = new Hud("DEMO");
        this.soundManager.init();
        this.soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        configureSounds();
    }

    private void configureSounds() {
        final SoundBuffer backgroundMusicBuffer = new SoundBuffer("assets/game/sounds/background.ogg");
        this.soundManager.addSoundBuffer(backgroundMusicBuffer);
        final SoundSource sourceBack = new SoundSource(true, true);
        sourceBack.setBuffer(backgroundMusicBuffer.getBufferId());
        this.soundManager.addSoundSource(Sounds.MUSIC.toString(), sourceBack);

        final SoundBuffer fireAmbienceBuffer = new SoundBuffer("assets/game/sounds/fire.ogg");
        this.soundManager.addSoundBuffer(fireAmbienceBuffer);
        final SoundSource sourceFire = new SoundSource(true, false);
        sourceFire.setPosition(this.particleEmitter.getBaseParticle().getPosition());
        sourceFire.setBuffer(fireAmbienceBuffer.getBufferId());
        this.soundManager.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();

        this.soundManager.setListener(new SoundListener(new Vector3f()));

        sourceBack.play();
    }

    private void configureLights() {
        final SceneLight sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 1.0f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(10);
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
        if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL) || window.isKeyPressed(GLFW_KEY_RIGHT_CONTROL)) {
            this.accelerationMultiplier = CAMERA_POS_STEP_ACCELERATED;
        } else {
            this.accelerationMultiplier = CAMERA_POS_STEP;
        }
    }

    @Override
    public void update(final float interval,
                       final MouseInput mouseInput) {
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotVec = mouseInput.getDisplVec();
            this.camera.moveRotation(
                    rotVec.x * MOUSE_SENSITIVITY,
                    rotVec.y * MOUSE_SENSITIVITY,
                    0
            );
        }

        final Vector3f prevPos = new Vector3f(this.camera.getPosition());
        this.camera.movePosition(
                this.cameraInc.x * this.accelerationMultiplier,
                this.cameraInc.y * this.accelerationMultiplier,
                this.cameraInc.z * this.accelerationMultiplier
        );

        final float height = this.terrain != null ? this.terrain.getHeight(this.camera.getPosition()) : -Float.MAX_VALUE;
        if (this.camera.getPosition().y <= height) {
            this.camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

        this.lightAngle = Math.min(Math.max(this.lightAngle + this.angleInc, 0), 180);
        final float zValue = (float) Math.cos(Math.toRadians(this.lightAngle));
        final float yValue = (float) Math.sin(Math.toRadians(this.lightAngle));
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        this.particleEmitter.update((long) (interval * 1000));
        this.soundManager.updateListenerPosition(this.camera);
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
        this.soundManager.cleanup();
        this.scene.cleanup();
        if (this.hud != null) {
            this.hud.cleanup();
        }
    }
}