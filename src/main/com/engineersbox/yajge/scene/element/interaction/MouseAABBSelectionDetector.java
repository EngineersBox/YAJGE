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
    private final Vector4f tmpVec;

    public MouseAABBSelectionDetector() {
        super();
        this.invProjectionMatrix = new Matrix4f();
        this.invViewMatrix = new Matrix4f();
        this.mouseDir = new Vector3f();
        this.tmpVec = new Vector4f();
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

        this.invProjectionMatrix.set(window.getProjectionMatrix()).invert();

        this.tmpVec.set(x, y, z, 1.0f).mul(this.invProjectionMatrix);
        this.tmpVec.z = -1.0f;
        this.tmpVec.w = 0.0f;
        
        this.invViewMatrix.set(camera.getViewMatrix());
        this.invViewMatrix.invert();
        this.tmpVec.mul(this.invViewMatrix);

        this.mouseDir.set(
                this.tmpVec.x,
                this.tmpVec.y,
                this.tmpVec.z
        );

        return selectSceneElement(
                sceneElements,
                camera.getPosition(),
                this.mouseDir
        );
    }
}
