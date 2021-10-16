package com.engineersbox.yajge.element.object;

import com.engineersbox.yajge.rendering.primitive.Mesh;
import org.joml.Vector3f;

public class SceneObject {
    private final Mesh mesh;
    private final Vector3f position;
    private float scale;
    private final Vector3f rotation;

    public SceneObject(final Mesh mesh) {
        this.mesh = mesh;
        this.position = new Vector3f();
        this.scale = 1;
        this.rotation = new Vector3f();
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(final float x,
                            final float y,
                            final float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(final float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public void setRotation(final float x,
                            final float y,
                            final float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh() {
        return this.mesh;
    }
}
