package com.engineersbox.yajge.input;

import com.engineersbox.yajge.engine.core.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    private final Vector2d previousPos;
    private final Vector2d currentPos;
    private final Vector2f displayVec;
    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    public MouseInput() {
        this.previousPos = new Vector2d(-1, -1);
        this.currentPos = new Vector2d(0, 0);
        this.displayVec = new Vector2f();
    }

    public void init(final Window window) {
        glfwSetCursorPosCallback(window.getWindowHandle(), (final long windowHandle, final double xpos, final double ypos) -> {
            this.currentPos.x = xpos;
            this.currentPos.y = ypos;
        });
        glfwSetCursorEnterCallback(window.getWindowHandle(), (final long windowHandle, final boolean entered) -> this.inWindow = entered);
        glfwSetMouseButtonCallback(window.getWindowHandle(), (final long windowHandle, final int button, final int action, final int mode) -> {
            this.leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            this.rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getDisplayVec() {
        return this.displayVec;
    }

    public void input(final Window window) {
        this.displayVec.x = 0;
        this.displayVec.y = 0;
        if (this.previousPos.x > 0 && this.previousPos.y > 0 && this.inWindow) {
            final double deltaX = this.currentPos.x - this.previousPos.x;
            final double deltaY = this.currentPos.y - this.previousPos.y;
            final boolean rotateX = deltaX != 0;
            final boolean rotateY = deltaY != 0;
            if (rotateX) {
                this.displayVec.y = (float) deltaX;
            }
            if (rotateY) {
                this.displayVec.x = (float) deltaY;
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
}
