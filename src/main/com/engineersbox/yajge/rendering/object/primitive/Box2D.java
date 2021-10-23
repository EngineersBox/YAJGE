package com.engineersbox.yajge.rendering.object.primitive;

public class Box2D {

    public float x;
    public float y;
    public float width;
    public float height;

    public Box2D(final float x,
                 final float y,
                 final float width,
                 final float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(final float xPos,
                            final float yPos) {
        return xPos >= x
                && yPos >= y
                && xPos < x + width
                && yPos < y + height;
    }
}
