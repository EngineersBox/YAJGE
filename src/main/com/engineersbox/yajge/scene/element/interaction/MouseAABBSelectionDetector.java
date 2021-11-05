package com.engineersbox.yajge.scene.element.interaction;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MouseAABBSelectionDetector extends AABBSelectionDetector {
    private final Matrix4f invProjectionMatrix;
    private final Matrix4f invViewMatrix;
    private final Vector3f mouseDir;
    private final Vector4f finalizedDirectionVec;

    public MouseAABBSelectionDetector() {
        super();
        this.invProjectionMatrix = new Matrix4f();
        this.invViewMatrix = new Matrix4f();
        this.mouseDir = new Vector3f();
        this.finalizedDirectionVec = new Vector4f();
    }

    public boolean selectSceneElement(final SceneElement[] sceneElements,
                                      final Window window,
                                      final Vector2d mousePos,
                                      final Camera camera) {
        final int wdwWitdh = window.getWidth();
        final int wdwHeight = window.getHeight();

        final float x = (float)(2 * mousePos.x) / (float) wdwWitdh - 1.0f;
        final float y = 1.0f - (float)(2 * mousePos.y) / (float) wdwHeight;
        final float z = -1.0f;

        this.invProjectionMatrix.set(window.getProjectionMatrix());
        this.invProjectionMatrix.invert();

        this.finalizedDirectionVec.set(x, y, z, 1.0f);
        this.finalizedDirectionVec.mul(this.invProjectionMatrix);
        this.finalizedDirectionVec.z = -1.0f;
        this.finalizedDirectionVec.w = 0.0f;

        final Matrix4f viewMatrix = camera.getViewMatrix();
        this.invViewMatrix.set(viewMatrix);
        this.invViewMatrix.invert();
        this.finalizedDirectionVec.mul(this.invViewMatrix);

        this.mouseDir.set(this.finalizedDirectionVec.x, this.finalizedDirectionVec.y, this.finalizedDirectionVec.z);

        return super.selectSceneElement(sceneElements, camera.getPosition(), this.mouseDir);
    }
}
