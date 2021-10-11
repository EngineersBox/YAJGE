package com.engineersbox.yajge.engine;

import com.engineersbox.yajge.rendering.Renderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.PrintStream;

public class Engine {

    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Renderer renderer;

    private boolean running = false;
    private boolean paused = false;
    private boolean shutdownRequested = false;

    public Engine() {
        init();
        this.renderer = new Renderer();
    }

    @SuppressWarnings("java:S2095")
    private void init() {
        GLFWErrorCallback.createPrint(new PrintStream(IoBuilder.forLogger(LOGGER).buildOutputStream())).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }

    public void run() {
        if (this.running) {
            return;
        }
        while (!shutdownRequested) {
            // TODO: Call updates to various engine elements here
            this.shutdownRequested = this.renderer.run();
        }
    }

    public void pause() {
        if (!this.running) {
            return;
        }
    }

    public void resume() {
        if (!this.running) {
            return;
        }
    }

    public void shutdown() {
        if (!this.running) {
            return;
        }
        this.shutdownRequested = true;
    }

    public boolean isPaused() {
        return isRunning() && this.paused;
    }

    public boolean isRunning() {
        return this.running;
    }
}
