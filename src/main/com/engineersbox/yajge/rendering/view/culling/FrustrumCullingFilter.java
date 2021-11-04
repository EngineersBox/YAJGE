package com.engineersbox.yajge.rendering.view.culling;

import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class FrustrumCullingFilter {

    private final Matrix4f projectionViewMatrix;
    private final FrustumIntersection frustumIntersection;

    public FrustrumCullingFilter() {
        this.projectionViewMatrix = new Matrix4f();
        this.frustumIntersection = new FrustumIntersection();
    }

    public void updateFrustum(final Matrix4f projMatrix,
                              final Matrix4f viewMatrix) {
        this.projectionViewMatrix.set(projMatrix).mul(viewMatrix);
        this.frustumIntersection.set(this.projectionViewMatrix);
    }

    public void filter(final Map<? extends Mesh, List<SceneElement>> mapMesh) {
        for (final Map.Entry<? extends Mesh, List<SceneElement>> entry : mapMesh.entrySet()) {
            filter(entry.getValue(), entry.getKey().getBoundingRadius());
        }
    }

    public void filter(final List<SceneElement> sceneElements,
                       final float meshBoundingRadius) {
        sceneElements.stream()
                .filter(SceneElement::frustrumCullingEnabled)
                .forEach((final SceneElement sceneElement) -> sceneElement.setInsideFrustrum(
                        insideFrustum(
                                sceneElement.getPosition().x,
                                sceneElement.getPosition().y,
                                sceneElement.getPosition().z,
                                sceneElement.getScale() * meshBoundingRadius
                        )
                ));
    }

    public boolean insideFrustum(final float x0,
                                 final float y0,
                                 final float z0,
                                 final float boundingRadius) {
        return this.frustumIntersection.testSphere(x0, y0, z0, boundingRadius);
    }
}
