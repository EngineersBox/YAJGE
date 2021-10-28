package com.engineersbox.yajge.rendering.object.md5.primitive;

public class MD5Triangle {

    private int index;
    private int vertex0;
    private int vertex1;
    private int vertex2;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getVertex0() {
        return this.vertex0;
    }

    public void setVertex0(final int vertex0) {
        this.vertex0 = vertex0;
    }

    public int getVertex1() {
        return this.vertex1;
    }

    public void setVertex1(final int vertex1) {
        this.vertex1 = vertex1;
    }

    public int getVertex2() {
        return this.vertex2;
    }

    public void setVertex2(final int vertex2) {
        this.vertex2 = vertex2;
    }

    @Override
    public String toString() {
        return "[index: " + this.index + ", vertex0: " + this.vertex0
                + ", vertex1: " + this.vertex1 + ", vertex2: " + this.vertex2 + "]";
    }
}
