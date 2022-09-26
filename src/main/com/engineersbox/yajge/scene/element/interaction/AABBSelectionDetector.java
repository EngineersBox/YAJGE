package com.engineersbox.yajge.scene.element.interaction;

import com.engineersbox.yajge.rendering.view.Camera;
import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class AABBSelectionDetector {

    private final Vector3f max;
    private final Vector3f min;
    private final Vector2f nearFar;
    private Vector3f dir;

    public AABBSelectionDetector() {
        this.dir = new Vector3f();
        this.min = new Vector3f();
        this.max = new Vector3f();
        this.nearFar = new Vector2f();
    }

    public void selectSceneElement(final SceneElement[] sceneElements,
                                   final Camera camera) {
        this.dir = camera.getViewMatrix()
                .positiveZ(this.dir)
                .negate();
        selectSceneElement(
                sceneElements,
                camera.getPosition(),
                this.dir
        );
    }
    
    protected boolean selectSceneElement(final SceneElement[] sceneElements,
                                         final Vector3f center,
                                         final Vector3f dir) {
        boolean selected = false;
        SceneElement selectedSceneElement = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (final SceneElement sceneElement : sceneElements) {
            sceneElement.setSelected(false);
            this.min.set(sceneElement.getPosition());
            this.max.set(sceneElement.getPosition());
            this.min.add(-sceneElement.getScale(), -sceneElement.getScale(), -sceneElement.getScale());
            this.max.add(sceneElement.getScale(), sceneElement.getScale(), sceneElement.getScale());
            if (Intersectionf.intersectRayAab(
                    center,
                    dir,
                    this.min,
                    this.max,
                    this.nearFar
            ) && this.nearFar.x < closestDistance) {
                closestDistance = this.nearFar.x;
                selectedSceneElement = sceneElement;
            }
        }
        if (selectedSceneElement != null) {
            selectedSceneElement.setSelected(true);
            selected = true;
        }
        return selected;
    }
}
