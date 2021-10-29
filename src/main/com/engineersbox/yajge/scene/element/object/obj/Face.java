package com.engineersbox.yajge.scene.element.object.obj;

public class Face {

    private final IdxGroup[] idxGroups;

    public Face(final String v1, final String v2, final String v3) {
        this.idxGroups = new IdxGroup[3];
        this.idxGroups[0] = parseLine(v1);
        this.idxGroups[1] = parseLine(v2);
        this.idxGroups[2] = parseLine(v3);
    }

    private IdxGroup parseLine(final String line) {
        final IdxGroup idxGroup = new IdxGroup();
        final String[] lineTokens = line.split("/");
        final int length = lineTokens.length;
        idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
        if (length > 1) {
            final String textCoord = lineTokens[1];
            idxGroup.idxTextCoord = !textCoord.isEmpty() ? Integer.parseInt(textCoord) - 1 : IdxGroup.NO_VALUE;
            if (length > 2) {
                idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
            }
        }

        return idxGroup;
    }

    public IdxGroup[] getFaceVertexIndices() {
        return this.idxGroups;
    }
}
