package com.engineersbox.yajge.input;

import com.engineersbox.yajge.core.window.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    private final Vector2d previousPos;
    private final Vector2d currentPos;
    private final Vector2f displVec;
    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    public MouseInput() {
        this.previousPos = new Vector2d(-1, -1);
        this.currentPos = new Vector2d(0, 0);
        this.displVec = new Vector2f();
    }

    public void init(final Window window) {
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
            this.currentPos.x = xpos;
            this.currentPos.y = ypos;
        });
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            this.inWindow = entered;
        });
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            this.leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            this.rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getDisplayVec() {
        return this.displVec;
    }

    public void input(final Window window) {
        this.displVec.x = 0;
        this.displVec.y = 0;
        if (this.previousPos.x > 0 && this.previousPos.y > 0 && this.inWindow) {
            final double deltaX = this.currentPos.x - this.previousPos.x;
            final double deltaY = this.currentPos.y - this.previousPos.y;
            if (deltaX != 0) {
                this.displVec.y = (float) deltaX;
            }
            if (deltaY != 0) {
                this.displVec.x = (float) deltaY;
            }
        }
        this.previousPos.x = this.currentPos.x;
        this.previousPos.y = this.currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return this.leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return this.rightButtonPressed;
    }

    public Vector2d getCurrentPos() {
        return this.currentPos;
    }
}
