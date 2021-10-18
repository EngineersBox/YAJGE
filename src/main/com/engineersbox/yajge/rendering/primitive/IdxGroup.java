package com.engineersbox.yajge.rendering.primitive;

public final class IdxGroup {

    public static final int OBJ_NO_VALUE = -1;

    public int idxPos;
    public int idxTextCoord;
    public int idxVecNormal;

    public IdxGroup(final int idxPos,
                    final int idxTextCoord,
                    final int idxVecNormal) {
        this.idxPos = idxPos;
        this.idxTextCoord = idxTextCoord;
        this.idxVecNormal = idxVecNormal;
    }

    public IdxGroup() {
        this(OBJ_NO_VALUE, OBJ_NO_VALUE, OBJ_NO_VALUE);
    }

}
