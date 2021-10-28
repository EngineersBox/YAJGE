package com.engineersbox.yajge.rendering.object.md5.hierarchy;

import java.util.ArrayList;
import java.util.List;

public class MD5Hierarchy {

    private List<MD5HierarchyData> hierarchyDataList;

    public List<MD5HierarchyData> getHierarchyDataList() {
        return this.hierarchyDataList;
    }

    public void setHierarchyDataList(final List<MD5HierarchyData> hierarchyDataList) {
        this.hierarchyDataList = hierarchyDataList;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("hierarchy [" + System.lineSeparator());
        for (final MD5HierarchyData hierarchyData : this.hierarchyDataList) {
            str.append(hierarchyData).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());
        return str.toString();
    }

    public static MD5Hierarchy parse(final List<String> blockBody) {
        final MD5Hierarchy result = new MD5Hierarchy();
        final List<MD5HierarchyData> hierarchyDataList = new ArrayList<>();
        result.setHierarchyDataList(hierarchyDataList);
        for (final String line : blockBody) {
            final MD5HierarchyData hierarchyData = MD5HierarchyData.parseLine(line);
            if (hierarchyData != null) {
                hierarchyDataList.add(hierarchyData);
            }
        }
        return result;
    }

}
