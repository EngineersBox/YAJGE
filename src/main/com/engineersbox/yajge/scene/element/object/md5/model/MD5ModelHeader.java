package com.engineersbox.yajge.scene.element.object.md5.model;

import java.util.List;

public class MD5ModelHeader {

    private String version;
    private String commandLine;
    private int numJoints;
    private int numMeshes;

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

    public int getNumJoints() {
        return this.numJoints;
    }

    public void setNumJoints(final int numJoints) {
        this.numJoints = numJoints;
    }

    public int getNumMeshes() {
        return this.numMeshes;
    }

    public void setNumMeshes(final int numMeshes) {
        this.numMeshes = numMeshes;
    }
    
    @Override
    public String toString() {
        return "[version: " + this.version + ", commandLine: " + this.commandLine +
                ", numJoints: " + this.numJoints + ", numMeshes: " + this.numMeshes + "]";
    }

    public static MD5ModelHeader parse(final List<String> lines) {
        final MD5ModelHeader header = new MD5ModelHeader();
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
                case "numJoints" -> header.setNumJoints(Integer.parseInt(tokens[1]));
                case "numMeshes" -> header.setNumMeshes(Integer.parseInt(tokens[1]));
                case "joints" -> finishHeader = true;
            }
        }

        return header;        
    }    
}
