package com.engineersbox.yajge.rendering.object.md5.bound;

import java.util.ArrayList;
import java.util.List;

public class MD5BoundInfo {

    private List<MD5Bound> bounds;

    public List<MD5Bound> getBounds() {
        return this.bounds;
    }

    public void setBounds(final List<MD5Bound> bounds) {
        this.bounds = bounds;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("bounds [" + System.lineSeparator());
        for (final MD5Bound bound : this.bounds) {
            str.append(bound).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static MD5BoundInfo parse(final List<String> blockBody) {
        final MD5BoundInfo result = new MD5BoundInfo();
        final List<MD5Bound> bounds = new ArrayList<>();
        for (final String line : blockBody) {
            final MD5Bound bound = MD5Bound.parseLine(line);
            if (bound != null) {
                bounds.add(bound);
            }
        }
        result.setBounds(bounds);
        return result;
    }
}
