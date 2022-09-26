package com.engineersbox.yajge.rendering.view.culling;

import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;
    private final FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        this.prjViewMatrix = new Matrix4f();
        this.frustumInt = new FrustumIntersection();
    }

    public void updateFrustum(final Matrix4f projMatrix,
                              final Matrix4f viewMatrix) {
        this.prjViewMatrix.set(projMatrix);
        this.prjViewMatrix.mul(viewMatrix);
        this.frustumInt.set(this.prjViewMatrix);
    }

    public void filter(final Map<? extends Mesh, List<SceneElement>> mapMesh) {
        for (final Map.Entry<? extends Mesh, List<SceneElement>> entry : mapMesh.entrySet()) {
            final List<SceneElement> sceneElements = entry.getValue();
            filter(sceneElements, entry.getKey().getBoundingRadius());
        }
    }

    public void filter(final List<SceneElement> sceneElements,
                       final float meshBoundingRadius) {
        float boundingRadius;
        Vector3f pos;
        for (final SceneElement sceneElement : sceneElements) {
            if (!sceneElement.isFrustumCullingDisabled()) {
                boundingRadius = sceneElement.getScale() * meshBoundingRadius;
                pos = sceneElement.getPosition();
                sceneElement.setInsideFrustum(insideFrustum(pos.x, pos.y, pos.z, boundingRadius));
            }
        }
    }

    public boolean insideFrustum(final float x0,
                                 final float y0,
                                 final float z0,
                                 final float boundingRadius) {
        return this.frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }
}
