package com.engineersbox.yajge.rendering.primitive;

public final class Face {

    private static final String OBJ_SECTION_DELIMITER = "/";
    private final IdxGroup[] idxGroups;

    public Face(String vertex1, String vertex2, String vertex3) {
        this.idxGroups = new IdxGroup[3];
        this.idxGroups[0] = parseLine(vertex1);
        this.idxGroups[1] = parseLine(vertex2);
        this.idxGroups[2] = parseLine(vertex3);
    }

    private IdxGroup parseLine(String line) {
        final IdxGroup idxGroup = new IdxGroup();
        final String[] lineTokens = line.split(OBJ_SECTION_DELIMITER);
        idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
        if (lineTokens.length > 1) {
            final String textCoord = lineTokens[1];
            idxGroup.idxTextCoord = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : IdxGroup.OBJ_NO_VALUE;
            if (lineTokens.length > 2) {
                idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
            }
        }
        return idxGroup;
    }

    public IdxGroup[] getFaceVertexIndices() {
        return this.idxGroups;
    }
}
