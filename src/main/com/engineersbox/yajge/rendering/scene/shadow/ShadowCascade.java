package com.engineersbox.yajge.rendering.scene.shadow;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

public class ShadowCascade {

    private static final int FRUSTUM_CORNERS = 8;

    private final Matrix4f projViewMatrix;
    private final Matrix4f orthoProjMatrix;
    private final Matrix4f lightViewMatrix;
    private final Vector3f centroid;
    private final Vector3f[] frustumCorners;
    private final float zNear;
    private final float zFar;
    private final Vector4f tmpVec;
    
    public ShadowCascade(final float zNear, final float zFar) {
        this.zNear = zNear;
        this.zFar = zFar;
        this.projViewMatrix = new Matrix4f();
        this.orthoProjMatrix = new Matrix4f();
        this.centroid = new Vector3f();
        this.lightViewMatrix = new Matrix4f();
        this.frustumCorners = new Vector3f[FRUSTUM_CORNERS];
        Arrays.setAll(this.frustumCorners, Vector3f::new);
        this.tmpVec = new Vector4f();
    }

    public Matrix4f getLightViewMatrix() {
        return this.lightViewMatrix;
    }

    public Matrix4f getOrthoProjMatrix() {
        return this.orthoProjMatrix;
    }

    public void update(final Window window, final Matrix4f viewMatrix, final DirectionalLight light) {
        final float aspectRatio = (float) window.getWidth() / (float) window.getHeight();
        this.projViewMatrix.setPerspective(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                aspectRatio,
                this.zNear,
                this.zFar
        );
        this.projViewMatrix.mul(viewMatrix);

        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            final Vector3f corner = this.frustumCorners[i];
            corner.set(0, 0, 0);
            this.projViewMatrix.frustumCorner(i, corner);
            this.centroid.add(corner);
            this.centroid.div(8.0f);
            minZ = Math.min(minZ, corner.z);
            maxZ = Math.max(maxZ, corner.z);
        }

        final Vector3f lightDirection = light.getDirection();
        final Vector3f lightPosInc = new Vector3f().set(lightDirection);
        lightPosInc.mul(maxZ - minZ);
        final Vector3f lightPosition = new Vector3f();
        lightPosition.set(this.centroid);
        lightPosition.add(lightPosInc);

        updateLightViewMatrix(lightDirection, lightPosition);
        updateLightProjectionMatrix();
    }

    private void updateLightViewMatrix(final Vector3f lightDirection,
                                       final Vector3f lightPosition) {
        Transform.updateGenericViewMatrix(
                lightPosition,
                new Vector3f(
                        (float) Math.toDegrees(Math.acos(lightDirection.z)),
                        (float) Math.toDegrees(Math.asin(lightDirection.x)),
                        0
                ),
                this.lightViewMatrix
        );
    }

    private void updateLightProjectionMatrix() {
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ =  Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            final Vector3f corner = this.frustumCorners[i];
            this.tmpVec.set(corner, 1);
            this.tmpVec.mul(this.lightViewMatrix);
            minX = Math.min(this.tmpVec.x, minX);
            maxX = Math.max(this.tmpVec.x, maxX);
            minY = Math.min(this.tmpVec.y, minY);
            maxY = Math.max(this.tmpVec.y, maxY);
            minZ = Math.min(this.tmpVec.z, minZ);
            maxZ = Math.max(this.tmpVec.z, maxZ);
        }
        this.orthoProjMatrix.setOrtho(
                minX,
                maxX,
                minY,
                maxY,
                0,
                maxZ - minZ
        );
    }

}
