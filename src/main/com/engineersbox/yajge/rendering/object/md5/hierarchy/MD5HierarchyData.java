package com.engineersbox.yajge.rendering.object.md5.hierarchy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5HierarchyData {

    private static final Pattern PATTERN_HIERARCHY = Pattern.compile("\\s*\\\"([^\\\"]+)\\\"\\s*([-]?\\d+)\\s*(\\d+)\\s*(\\d+).*");

    private String name;
    private int parentIndex;
    private int flags;
    private int startIndex;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getParentIndex() {
        return this.parentIndex;
    }

    public void setParentIndex(final int parentIndex) {
        this.parentIndex = parentIndex;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public String toString() {
        return "[name: " + this.name + ", parentIndex: " + this.parentIndex + ", flags: " + this.flags + ", startIndex: " + this.startIndex + "]";
    }

    public static MD5HierarchyData parseLine(final String line) {
        MD5HierarchyData result = null;
        final Matcher matcher = PATTERN_HIERARCHY.matcher(line);
        if (matcher.matches()) {
            result = new MD5HierarchyData();
            result.setName(matcher.group(1));
            result.setParentIndex(Integer.parseInt(matcher.group(2)));
            result.setFlags(Integer.parseInt(matcher.group(3)));
            result.setStartIndex(Integer.parseInt(matcher.group(4)));
        }
        return result;
    }

}
