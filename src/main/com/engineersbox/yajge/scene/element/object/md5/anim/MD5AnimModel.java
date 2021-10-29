package com.engineersbox.yajge.scene.element.object.md5.anim;

import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.scene.element.object.md5.bound.MD5BoundInfo;
import com.engineersbox.yajge.scene.element.object.md5.frame.MD5BaseFrame;
import com.engineersbox.yajge.scene.element.object.md5.frame.MD5Frame;
import com.engineersbox.yajge.scene.element.object.md5.hierarchy.MD5Hierarchy;

import java.util.ArrayList;
import java.util.List;

public class MD5AnimModel {

    private MD5AnimHeader header;
    private MD5Hierarchy hierarchy;
    private MD5BoundInfo boundInfo;
    private MD5BaseFrame baseFrame;
    private List<MD5Frame> frames;

    public MD5AnimModel() {
        this.frames = new ArrayList<>();
    }

    public MD5AnimHeader getHeader() {
        return this.header;
    }

    public void setHeader(final MD5AnimHeader header) {
        this.header = header;
    }

    public MD5Hierarchy getHierarchy() {
        return this.hierarchy;
    }

    public void setHierarchy(final MD5Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public MD5BoundInfo getBoundInfo() {
        return this.boundInfo;
    }

    public void setBoundInfo(final MD5BoundInfo boundInfo) {
        this.boundInfo = boundInfo;
    }

    public MD5BaseFrame getBaseFrame() {
        return this.baseFrame;
    }

    public void setBaseFrame(final MD5BaseFrame baseFrame) {
        this.baseFrame = baseFrame;
    }

    public List<MD5Frame> getFrames() {
        return this.frames;
    }

    public void setFrames(final List<MD5Frame> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("MD5AnimModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getHierarchy()).append(System.lineSeparator());
        str.append(getBoundInfo()).append(System.lineSeparator());
        str.append(getBaseFrame()).append(System.lineSeparator());

        for (final MD5Frame frame : this.frames) {
            str.append(frame).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static MD5AnimModel parse(final String animFile) {
        final List<String> lines = ResourceLoader.loadAsStringLines(animFile);

        final MD5AnimModel result = new MD5AnimModel();

        final int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new RuntimeException("Cannot parse empty file");
        }

        // Parse Header
        boolean headerEnd = false;
        int start = 0;
        for (int i = 0; i < numLines && !headerEnd; i++) {
            final String line = lines.get(i);
            headerEnd = line.trim().endsWith("{");
            start = i;
        }
        if (!headerEnd) {
            throw new RuntimeException("Cannot find header");
        }
        final List<String> headerBlock = lines.subList(0, start);
        final MD5AnimHeader header = MD5AnimHeader.parse(headerBlock);
        result.setHeader(header);

        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < numLines; i++) {
            final String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                final List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }

        return result;
    }

    private static void parseBlock(final MD5AnimModel model, final String blockId, final List<String> blockBody) {
        switch (blockId) {
            case "hierarchy":
                final MD5Hierarchy hierarchy = MD5Hierarchy.parse(blockBody);
                model.setHierarchy(hierarchy);
                break;
            case "bounds":
                final MD5BoundInfo boundInfo = MD5BoundInfo.parse(blockBody);
                model.setBoundInfo(boundInfo);
                break;
            case "baseframe":
                final MD5BaseFrame baseFrame = MD5BaseFrame.parse(blockBody);
                model.setBaseFrame(baseFrame);
                break;
            default:
                if (blockId.startsWith("frame ")) {
                    final MD5Frame frame = MD5Frame.parse(blockId, blockBody);
                    model.getFrames().add(frame);
                }
                break;
        }
    }
}
