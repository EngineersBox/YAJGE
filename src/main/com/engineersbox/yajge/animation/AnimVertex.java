package com.engineersbox.yajge.animation;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class AnimVertex {

    public Vector3f position;
    public Vector2f textCoords;
    public Vector3f normal;
    public float[] weights;
    public int[] jointIndices;

    public AnimVertex() {
        super();
        this.normal = new Vector3f();
    }
}
