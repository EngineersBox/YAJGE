package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.rendering.object.composite.Mesh;
import org.joml.Vector3f;

public class SceneElement {
    private Mesh[] meshes;
    private final Vector3f position;
    private float scale;
    private final Vector3f rotation;

    public SceneElement() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
    }

    public SceneElement(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public SceneElement(Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh() {
        return meshes[0];
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{mesh};
    }
}
