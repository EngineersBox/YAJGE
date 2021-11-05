package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;

public class SceneElement {

    private Mesh[] meshes;
    private final Vector3f position;
    private float scale;
    private final Quaternionf rotation;
    private int textPos;
    private boolean selected = false;
    private boolean insideFrustum = true;
    private boolean frustrumCullingEnabled = true;

    public SceneElement() {
        this.position = new Vector3f();
        this.scale = 1;
        this.rotation = new Quaternionf();
        this.textPos = 0;
    }

    public SceneElement(final Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public SceneElement(final Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public int getTextPos() {
        return this.textPos;
    }

    public final void setPosition(final float x,
                                  final float y,
                                  final float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return this.scale;
    }

    public final void setScale(final float scale) {
        this.scale = scale;
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public final void setRotation(final Quaternionf q) {
        this.rotation.set(q);
    }

    public Mesh getMesh() {
        return this.meshes[0];
    }

    public Mesh[] getMeshes() {
        return this.meshes;
    }

    public void setMeshes(final Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(final Mesh mesh) {
        this.meshes = new Mesh[]{mesh};
    }

    public void cleanup() {
        if (this.meshes == null) {
            return;
        }
        Arrays.stream(this.meshes).forEach(Mesh::cleanUp);
    }

    public void setTextPos(final int textPos) {
        this.textPos = textPos;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isInsideFrustum() {
        return this.insideFrustum;
    }

    public void setInsideFrustrum(final boolean insideFrustum) {
        this.insideFrustum = insideFrustum;
    }

    public boolean frustrumCullingEnabled() {
        return this.frustrumCullingEnabled;
    }

    public void setFrustumCulling(final boolean frustumCulling) {
        this.frustrumCullingEnabled = frustumCulling;
    }
}
