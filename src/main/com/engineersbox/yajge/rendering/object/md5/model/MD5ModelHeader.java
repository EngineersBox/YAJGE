package com.engineersbox.yajge.rendering.object.md5.model;

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
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new RuntimeException("Cannot parse empty file");
        }

        boolean finishHeader = false;
        for (int i = 0; i < numLines && !finishHeader; i++) {
            final String line = lines.get(i);
            final String[] tokens = line.split("\\s+");
            if (tokens.length > 1) {
                final String paramName = tokens[0];
                final String paramValue = tokens[1];

                switch (paramName) {
                    case "MD5Version" -> header.setVersion(paramValue);
                    case "commandline" -> header.setCommandLine(paramValue);
                    case "numJoints" -> header.setNumJoints(Integer.parseInt(paramValue));
                    case "numMeshes" -> header.setNumMeshes(Integer.parseInt(paramValue));
                    case "joints" -> finishHeader = true;
                }
            }
        }

        return header;        
    }    
}
