package com.engineersbox.yajge.rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Renderer {

    private final Window window;

    public Renderer() {
        // TODO: Initialise window based on configs
        this.window = new Window();
    }

    public boolean run() {
        this.window.show(); // Possibly defer showing window till after some init?
        if (this.window.shouldClose()) {
            this.window.destroy();
            return false;
        }
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        this.window.swapBuffers();
        GLFW.glfwPollEvents();
        return true;
    }

}
