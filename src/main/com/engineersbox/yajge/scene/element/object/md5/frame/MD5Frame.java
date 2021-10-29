package com.engineersbox.yajge.scene.element.object.md5.frame;

import com.engineersbox.yajge.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class MD5Frame {

    private int id;
    private float[] frameData;

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public float[] getFrameData() {
        return this.frameData;
    }

    public void setFrameData(final float[] frameData) {
        this.frameData = frameData;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("frame " + this.id + " [data: " + System.lineSeparator());
        for (final float frame : this.frameData) {
            str.append(frame).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());
        return str.toString();
    }

    public static MD5Frame parse(final String blockId, final List<String> blockBody) {
        final MD5Frame result = new MD5Frame();
        final String[] tokens = blockId.trim().split("\\s+");
        if (tokens.length >= 2) {
            result.setId(Integer.parseInt(tokens[1]));
        } else {
            throw new RuntimeException("Wrong frame definition: " + blockId);
        }

        final List<Float> data = new ArrayList<>();
        for (final String line : blockBody) {
            final List<Float> lineData = parseLine(line);
            data.addAll(lineData);
        }
        final float[] dataArr = ListUtils.floatListToArray(data);
        result.setFrameData(dataArr);

        return result;
    }

    private static List<Float> parseLine(final String line) {
        final String[] tokens = line.trim().split("\\s+");
        final List<Float> data = new ArrayList<>();
        for (final String token : tokens) {
            data.add(Float.parseFloat(token));
        }
        return data;
    }
}