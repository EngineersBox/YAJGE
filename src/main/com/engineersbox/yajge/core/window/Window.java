package com.engineersbox.yajge.core.window;

import com.engineersbox.yajge.logging.LoggerCompat;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
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
    private final WindowOptions opts;
    
    public Window(final String title,
                  final int width,
                  final int height,
                  final boolean vSync,
                  final WindowOptions opts) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.opts = opts;
    }

    private void configureHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        if (ConfigHandler.CONFIG.engine.glOptions.debugLogs) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE);
        }
    }

    private void configureCallbacks() {
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
        LoggerCompat.registerGLFWErrorLogger(LOGGER, Level.ERROR);
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        configureHints();
        boolean maximized = false;
        if (this.width == 0 || this.height == 0) {
            this.width = 100;
            this.height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximized = true;
        }
        this.windowHandle = glfwCreateWindow(
                this.width,
                this.height,
                this.title,
                NULL,
                NULL
        );
        if (this.windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        LOGGER.info("Using configured monitor {} [id: {}]", ConfigHandler.CONFIG.video.monitor, findMonitorByIndex(ConfigHandler.CONFIG.video.monitor));
        configureCallbacks();
        if (maximized) {
            glfwMaximizeWindow(this.windowHandle);
        } else {
            final GLFWVidMode vidmode = glfwGetVideoMode(findMonitorByIndex(ConfigHandler.CONFIG.video.monitor));
            glfwSetWindowPos(
                    this.windowHandle,
                    (vidmode.width() - this.width) / 2,
                    (vidmode.height() - this.height) / 2
            );
        }
        glfwMakeContextCurrent(this.windowHandle);
        if (isVSyncEnabled()) {
            glfwSwapInterval(1);
        }
        glfwShowWindow(this.windowHandle);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        if (this.opts.showTriangles()) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (this.opts.cullFace()) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    private long findMonitorByIndex(final int idx) {
        final PointerBuffer monitors = glfwGetMonitors();
        if (monitors == null || !monitors.hasRemaining()) {
            throw new RuntimeException("No monitors connected");
        }
        final int monitorsCount = monitors.limit();
        if (idx < 0 || idx >= monitorsCount) {
            throw new RuntimeException(String.format(
                    "Invalid monitor %d, must be one of []",
                    idx,
                    IntStream.range(0, monitorsCount)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","))
            ));
        }
        return monitors.get(idx);
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    public void setClearColor(final float r,
                              final float g,
                              final float b,
                              final float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public String getWindowTitle() {
        return this.title;
    }

    public void setWindowTitle(final String title) {
        glfwSetWindowTitle(this.windowHandle, title);
    }

        public boolean isKeyPressed(final int keyCode) {
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

    public void setResized(final boolean resized) {
        this.resized = resized;
    }

    public boolean isVSyncEnabled() {
        return this.vSync;
    }

    public void setvSync(final boolean vSync) {
        this.vSync = vSync;
    }

    public void update() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }

    public void cleanup() {
        glfwFreeCallbacks(this.windowHandle);
        glfwDestroyWindow(this.windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
