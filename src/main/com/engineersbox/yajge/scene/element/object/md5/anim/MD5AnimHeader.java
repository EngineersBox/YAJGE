package com.engineersbox.yajge.scene.element.object.md5.anim;

import java.util.List;

public class MD5AnimHeader {
    
    private String version;
    private String commandLine;
    private int numFrames;
    private int numJoints;
    private int frameRate;
    private int numAnimatedComponents;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getCommandLine() {
        return this.commandLine;
    }

    public void setCommandLine(final String commandLine) {
        this.commandLine = commandLine;
    }

    public int getNumFrames() {
        return this.numFrames;
    }

    public void setNumFrames(final int numFrames) {
        this.numFrames = numFrames;
    }

    public int getNumJoints() {
        return this.numJoints;
    }

    public void setNumJoints(final int numJoints) {
        this.numJoints = numJoints;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public void setFrameRate(final int frameRate) {
        this.frameRate = frameRate;
    }

    public int getNumAnimatedComponents() {
        return this.numAnimatedComponents;
    }

    public void setNumAnimatedComponents(final int numAnimatedComponents) {
        this.numAnimatedComponents = numAnimatedComponents;
    }
    
    @Override
    public String toString() {
        return "animHeader: [version: " + this.version + ", commandLine: " + this.commandLine +
                ", numFrames: " + this.numFrames + ", numJoints: " + this.numJoints +
                ", frameRate: " + this.frameRate + ", numAnimatedComponents:" + this.numAnimatedComponents + "]";
    }

    public static MD5AnimHeader parse(final List<String> lines)  {
        final MD5AnimHeader header = new MD5AnimHeader();
        final int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new RuntimeException("Cannot parse empty file");
        }
        boolean finishHeader = false;
        for (int i = 0; i < numLines && !finishHeader; i++) {
            final String[] tokens = lines.get(i).split("\\s+");
            if (tokens.length <= 1) {
                continue;
            }
            switch (tokens[0]) {
                case "MD5Version" -> header.setVersion(tokens[1]);
                case "commandline" -> header.setCommandLine(tokens[1]);
                case "numFrames" -> header.setNumFrames(Integer.parseInt(tokens[1]));
                case "numJoints" -> header.setNumJoints(Integer.parseInt(tokens[1]));
                case "frameRate" -> header.setFrameRate(Integer.parseInt(tokens[1]));
                case "numAnimatedComponents" -> header.setNumAnimatedComponents(Integer.parseInt(tokens[1]));
                case "hierarchy" -> finishHeader = true;
            }
        }
        return header;
    }
}
