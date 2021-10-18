package com.engineersbox.yajge.engine.core;

import static org.lwjgl.glfw.GLFW.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static final Logger LOGGER = LogManager.getLogger(Window.class);

    private final String title;
    private int width;
    private int height;
    private long windowHandle;
    private boolean resized;
    private boolean vSync;

    public Window(final String title,
                  final int width,
                  final int height,
                  final boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
    }

    private void configureHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    }

    private void createCallbacks() {
        glfwSetFramebufferSizeCallback(
                this.windowHandle,
                (final long window, final int width, final int height) -> {
                    this.width = width;
                    this.height = height;
                    this.setResized(true);
                }
        );
        glfwSetKeyCallback(
                this.windowHandle,
                (final long window, final int key, final int scancode, final int action, final int mods) -> {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                        glfwSetWindowShouldClose(window, true);
                    }
                }
        );
    }

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        configureHints();
        this.windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (this.windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window"); // TODO: Implement an exception for this
        }
        createCallbacks();
        final GLFWVidMode primaryMonitorResolution = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                windowHandle,
                (primaryMonitorResolution.width() - width) / 2,
                (primaryMonitorResolution.height() - height) / 2
        );
        glfwMakeContextCurrent(this.windowHandle);
        if (isVSyncEnabled()) {
            glfwSwapInterval(1);
        }
        glfwShowWindow(this.windowHandle);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
    }

    public void setClearColor(final float r,
                              final float g,
                              final float b,
                              final float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(this.windowHandle, keyCode) == GLFW_PRESS;
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(this.windowHandle);
    }

    public String getTitle() {
        return this.title;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isResized() {
        return this.resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isVSyncEnabled() {
        return this.vSync;
    }

    public void setVSync(boolean vSync) {
        this.vSync = vSync;
    }

    public void update() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }
}