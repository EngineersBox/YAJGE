package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.engine.core.IGameLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.rendering.Renderer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

public class TestGame implements IGameLogic {

    private int direction = 0;

    private float color = 0.0f;

    private final Renderer renderer;

    public TestGame() {
        this.renderer = new Renderer();
    }

    @Override
    public void init() throws Exception {
        this.renderer.init();
    }

    @Override
    public void input(final Window window) {
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            this.direction = 1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            this.direction = -1;
        } else {
            this.direction = 0;
        }
    }

    @Override
    public void update(final float interval) {
        this.color += this.direction * 0.01f;
        if (this.color > 1) {
            this.color = 1.0f;
        } else if (this.color < 0) {
            this.color = 0.0f;
        }
    }

    @Override
    public void render(final Window window) {
        window.setClearColor(this.color, this.color, this.color, 0.0f);
        this.renderer.render(window);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
    }
}
