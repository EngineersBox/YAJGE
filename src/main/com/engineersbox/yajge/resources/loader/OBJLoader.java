package com.engineersbox.yajge.resources.loader;

import com.engineersbox.yajge.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.object.primitive.obj.Face;
import com.engineersbox.yajge.scene.element.object.primitive.obj.IdxGroup;
import com.engineersbox.yajge.util.ListUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OBJLoader {

    public static Mesh loadMesh(final String fileName) {
        return loadMesh(fileName, 1);
    }

    public static Mesh loadMesh(final String fileName,
                                final int instances) {
        final List<String> lines = ResourceLoader.loadAsStringLines(fileName);
        
        final List<Vector3f> vertices = new ArrayList<>();
        final List<Vector2f> textures = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Face> faces = new ArrayList<>();

        for (final String line : lines) {
            final String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v" -> {
                    // Geometric vertex
                    final Vector3f vec3f = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vec3f);
                }
                case "vt" -> {
                    // Texture coordinate
                    final Vector2f vec2f = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vec2f);
                }
                case "vn" -> {
                    // Vertex normal
                    final Vector3f vec3fNorm = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vec3fNorm);
                }
                case "f" -> {
                    final Face face = new Face(tokens[1], tokens[2], tokens[3]);
                    faces.add(face);
                }
                default -> {
                }
                // Ignore other lines
            }
        }
        return reorderLists(
                vertices,
                textures,
                normals,
                faces,
                instances
        );
    }

    private static Mesh reorderLists(final List<Vector3f> positions,
                                     final List<Vector2f> texCoords,
                                     final List<Vector3f> normal,
                                     final List<Face> faces,
                                     final int instances) {

        final List<Integer> indices = new ArrayList<>();
        final float[] posArr = new float[positions.size() * 3];
        for (int i = 0; i < positions.size(); i++) {
            posArr[i * 3] = positions.get(i).x;
            posArr[i * 3 + 1] = positions.get(i).y;
            posArr[i * 3 + 2] = positions.get(i).z;
        }
        final float[] textCoordArr = new float[positions.size() * 2];
        final float[] normArr = new float[positions.size() * 3];

        for (final Face face : faces) {
            final IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (final IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(
                        indValue,
                        texCoords,
                        normal,
                        indices,
                        textCoordArr,
                        normArr
                );
            }
        }
        final int[] indicesArr = ListUtils.intListToArray(indices);
        final Mesh mesh;
        if (instances > 1) {
            mesh = new InstancedMesh(posArr, textCoordArr, normArr, indicesArr, instances);
        } else {
            mesh = new Mesh(posArr, textCoordArr, normArr, indicesArr);
        }
        return mesh;
    }

    private static void processFaceVertex(final IdxGroup indices,
                                          final List<Vector2f> texCoords,
                                          final List<Vector3f> normals,
                                          final List<Integer> indicesList,
                                          final float[] texCoordArr,
                                          final float[] normArr) {

        final int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        if (indices.idxTextCoord >= 0) {
            final Vector2f textCoord = texCoords.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord.x;
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
        }
        if (indices.idxVecNormal >= 0) {
            final Vector3f vecNorm = normals.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm.x;
            normArr[posIndex * 3 + 1] = vecNorm.y;
            normArr[posIndex * 3 + 2] = vecNorm.z;
        }
    }

}
