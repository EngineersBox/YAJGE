package com.engineersbox.yajge.resources.primitive;

import java.util.ArrayList;
import java.util.List;

import com.engineersbox.yajge.rendering.primitive.Face;
import com.engineersbox.yajge.rendering.primitive.IdxGroup;
import com.engineersbox.yajge.rendering.primitive.Mesh;
import com.engineersbox.yajge.resources.ResourceLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class OBJLoader {

    private static final String SPACES_REGEX = "\\s+";

    private static final String OBJ_GEOMETRIC_VERTEX_SYMBOL = "v";
    private static final String OBJ_TEXTURE_COORD_SYMBOL = "vt";
    private static final String OBJ_VERTEX_NORMAL_SYMBOL = "vn";
    private static final String OBJ_FACE_SYMBOL = "f";

    public static Mesh loadMesh(String fileName) {
        final List<String> lines = ResourceLoader.loadAsStringLines(fileName);

        final List<Vector3f> vertices = new ArrayList<>();
        final List<Vector2f> textures = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Face> faces = new ArrayList<>();

        for (final String line : lines) {
            final String[] tokens = line.split(SPACES_REGEX);
            switch (tokens[0]) {
                case OBJ_GEOMETRIC_VERTEX_SYMBOL -> vertices.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                ));
                case OBJ_TEXTURE_COORD_SYMBOL -> textures.add(new Vector2f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2])
                ));
                case OBJ_VERTEX_NORMAL_SYMBOL -> normals.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                ));
                case OBJ_FACE_SYMBOL -> faces.add(new Face(tokens[1], tokens[2], tokens[3]));
                default -> {}
            }
        }
        return reorderLists(vertices, textures, normals, faces);
    }

    private static Mesh reorderLists(final List<Vector3f> positions,
                                     final List<Vector2f> texCoords,
                                     final List<Vector3f> normals,
                                     final List<Face> faces) {

        final List<Integer> indices = new ArrayList<>();
        final float[] posArr = new float[positions.size() * 3];
        int i = 0;
        for (final Vector3f pos : positions) {
            posArr[i * 3] = pos.x;
            posArr[i * 3 + 1] = pos.y;
            posArr[i * 3 + 2] = pos.z;
            i++;
        }
        final float[] textCoordArr = new float[positions.size() * 2];
        final float[] normArr = new float[positions.size() * 3];

        for (final Face face : faces) {
            final IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (final IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(indValue, texCoords, normals,
                        indices, textCoordArr, normArr);
            }
        }
        int[] indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        return new Mesh(posArr, textCoordArr, normArr, indicesArr);
    }

    private static void processFaceVertex(final IdxGroup indices,
                                          final List<Vector2f> textCoordList,
                                          final List<Vector3f> normList,
                                          final List<Integer> indicesList,
                                          final float[] texCoordArr,
                                          final float[] normArr) {

        final int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        // Reorder texture coordinates
        if (indices.idxTextCoord >= 0) {
            final Vector2f textCoord = textCoordList.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord.x;
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
        }
        if (indices.idxVecNormal >= 0) {
            // Reorder normals
            final Vector3f vecNorm = normList.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm.x;
            normArr[posIndex * 3 + 1] = vecNorm.y;
            normArr[posIndex * 3 + 2] = vecNorm.z;
        }
    }
}