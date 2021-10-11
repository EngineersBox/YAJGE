package com.engineersbox.yajge.rendering;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class Window {

    private static final String DEFAULT_WINDOW_TITLE = "YAJGE";
    private static final int DEFAULT_WINDOW_WIDTH = 1024;
    private static final int DEFAULT_WINDOW_HEIGHT = 576;

    private final long id;
    private final String title;
    private final int width;
    private final int height;

    public Window() {
        this(DEFAULT_WINDOW_TITLE, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
    }
    public Window(final String title,
                  final int width,
                  final int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // TODO: Set monitor based on configs (4th parameter)
        this.id = GLFW.glfwCreateWindow(width, height, title, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
        if (this.id == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window"); // TODO: Create an exception for this
        }

        GLFW.glfwSetKeyCallback(this.id, (window, key, scancode, action, mods) -> {
            if ( key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE )
                GLFW.glfwSetWindowShouldClose(window, true);
        });

        // Get the thread stack and push a new frame
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer pWidth = stack.mallocInt(1);
            final IntBuffer pHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(this.id, pWidth, pHeight);
            final GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (vidMode == null) {
                throw new RuntimeException("Could not get video mode for primary monitor"); // TODO: Implement an exception for this
            }

            GLFW.glfwSetWindowPos(
                    this.id,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically
    }

    public void show() {
        GLFW.glfwMakeContextCurrent(this.id);
        GL.createCapabilities();
        GL11.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLFW.glfwShowWindow(id);
    }

    public void destroy() {
        Callbacks.glfwFreeCallbacks(this.id);
        GLFW.glfwDestroyWindow(this.id);
        GLFW.glfwTerminate();
        final GLFWErrorCallback errorCallback = GLFW.glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
            return;
        }
        throw new RuntimeException("Unable to free GLFW error callback"); // TODO: Implement an exception for this
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.id);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.id);
    }

    public long getId() {
        return this.id;
    }

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public String getTitle() {
        return this.title;
    }

}
