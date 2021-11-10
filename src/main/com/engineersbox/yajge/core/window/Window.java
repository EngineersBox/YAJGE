package com.engineersbox.yajge.core.window;

import com.engineersbox.yajge.logging.LoggerCompat;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final Matrix4f projectionMatrix;

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
        this.projectionMatrix = new Matrix4f();
    }

    private void configureHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (this.opts.compatibleProfile()) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }
    }

    private void configureCallbacks() {
        glfwSetFramebufferSizeCallback(this.windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });
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
        this.windowHandle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        final long monitorId = findMonitorByIndex(ConfigHandler.CONFIG.video.monitor);
        configureCallbacks();
        if (maximized) {
            glfwMaximizeWindow(this.windowHandle);
        } else {
            final GLFWVidMode videoMode = glfwGetVideoMode(monitorId);
            if (videoMode == null) {
                throw new RuntimeException(String.format(
                        "Failed to get video mode for monitor %d",
                        ConfigHandler.CONFIG.video.monitor
                ));
            }
            glfwSetWindowPos(
                    this.windowHandle,
                    (videoMode.width() - this.width) / 2,
                    (videoMode.height() - this.height) / 2
            );
        }
        LOGGER.debug("Using configured monitor {} [id: {}]", ConfigHandler.CONFIG.video.monitor, monitorId);

        glfwMakeContextCurrent(this.windowHandle);
        if (isvSync()) {
            glfwSwapInterval(1);
        }

        glfwShowWindow(this.windowHandle);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        if (this.opts.showTriangles()) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        if (this.opts.cullFace()) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
        if (this.opts.antialiasing()) {
            glfwWindowHint(GLFW_SAMPLES, 4);
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
                    "Invalid monitor %d, must be one of [%s]",
                    idx,
                    IntStream.range(0, monitorsCount)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","))
            ));
        }
        return monitors.get(idx);
    }
    
    public void restoreState() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (ConfigHandler.CONFIG.engine.glOptions.cullface) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    public String getWindowTitle() {
        return this.title;
    }

    public void setWindowTitle(final String title) {
        glfwSetWindowTitle(this.windowHandle, title);
    }

    public WindowOptions getWindowOptions() {
        return this.opts;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix() {
        final float aspectRatio = (float) this.width / (float) this.height;
        return this.projectionMatrix.setPerspective(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                aspectRatio,
                (float) ConfigHandler.CONFIG.render.camera.zNear,
                (float) ConfigHandler.CONFIG.render.camera.zFar
        );
    }

    public static Matrix4f updateProjectionMatrix(final Matrix4f matrix,
                                                  final int width,
                                                  final int height) {
        final float aspectRatio = (float) width / (float) height;
        return matrix.setPerspective(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                aspectRatio,
                (float) ConfigHandler.CONFIG.render.camera.zNear,
                (float) ConfigHandler.CONFIG.render.camera.zFar
        );
    }

    public void setClearColor(final float r,
                              final float g,
                              final float b,
                              final float a) {
        glClearColor(r, g, b, a);
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

    public boolean isvSync() {
        return this.vSync;
    }

    public void setvSync(final boolean vSync) {
        this.vSync = vSync;
    }

    public void update() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }

    public WindowOptions getOptions() {
        return this.opts;
    }

}
