package com.engineersbox.yajge.scene.element.object.primitive;

public class Box2D {
    public final float x;
    public final float y;
    public final float width;
    public final float height;

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
        return xPos >= this.x
                && yPos >= this.y
                && xPos < this.x + this.width
                && yPos < this.y + this.height;
    }
}
