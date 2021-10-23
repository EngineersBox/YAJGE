package com.engineersbox.yajge.rendering.object.composite;

import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.rendering.assets.materials.Texture;
import com.engineersbox.yajge.util.ListUtils;
import org.joml.Vector3f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class HeightMapMesh {

    private static final int MAX_CHANNEL_VALUE = 255;
    private static final int MAX_COLOUR = MAX_CHANNEL_VALUE * MAX_CHANNEL_VALUE * MAX_CHANNEL_VALUE;
    public static final float STARTX = -0.5f;
    public static final float STARTZ = -0.5f;

    private final float minY;
    private final float maxY;
    private final Mesh mesh;
    private final float[][] heightArray;

    public HeightMapMesh(final float minY,
                         final float maxY,
                         final ByteBuffer heightMapImage,
                         final int width,
                         final int height,
                         final String textureFile,
                         final int texInc) {
        this.minY = minY;
        this.maxY = maxY;
        this.heightArray = new float[height][width];
        final Texture texture = new Texture(textureFile);
        this.mesh = buildMesh(width, height, heightMapImage, texInc);
        this.mesh.setMaterial(new Material(texture, 0.0f));
    }

    private Mesh buildMesh(final int width,
                           final int height,
                           final ByteBuffer heightMapImage,
                           final int texInc) {
        final float incX = getXLength() / (width - 1);
        final float incZ = getZLength() / (height - 1);
        final List<Float> positions = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                positions.add(STARTX + col * incX);
                final float currentHeight = getHeight(col, row, width, heightMapImage);
                this.heightArray[row][col] = currentHeight;
                positions.add(currentHeight);
                positions.add(STARTZ + row * incZ);

                texCoords.add((float) texInc * (float) col / (float) width);
                texCoords.add((float) texInc * (float) row / (float) height);

                if (col < width - 1 && row < height - 1) {
                    int leftTop = row * width + col;
                    int leftBottom = (row + 1) * width + col;
                    int rightBottom = (row + 1) * width + col + 1;
                    int rightTop = row * width + col + 1;

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);
                }
            }
        }
        final float[] posArr = ListUtils.floatListToArray(positions);
        final float[] texCoordsArr = ListUtils.floatListToArray(texCoords);
        final int[] indicesArr = ListUtils.intListToArray(indices);
        final float[] normalsArr = calcNormals(posArr, width, height);
        return new Mesh(posArr, texCoordsArr, normalsArr, indicesArr);
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    public float getHeight(int row, int col) {
        if ((row >= 0 && row < this.heightArray.length)
            && (col >= 0 && col < this.heightArray[row].length)) {
            return this.heightArray[row][col];
        }
        return 0;
    }

    public static float getXLength() {
        return Math.abs(-STARTX * 2);
    }

    public static float getZLength() {
        return Math.abs(-STARTZ * 2);
    }

    private float[] calcNormals(final float[] posArr,
                                final int width,
                                final int height) {
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        final Vector3f v12 = new Vector3f();
        final Vector3f v23 = new Vector3f();
        final Vector3f v34 = new Vector3f();
        final Vector3f v41 = new Vector3f();
        final List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    assignVertexPosition(v0, row, col, width, posArr);
                    assignVertexPosition(v1, row, col - 1, width, posArr);
                    v1 = v1.sub(v0);
                    assignVertexPosition(v2, row + 1, col, width, posArr);
                    v2 = v2.sub(v0);
                    assignVertexPosition(v3, row, col + 1, width, posArr);
                    v3 = v3.sub(v0);
                    assignVertexPosition(v4, row - 1, col, width, posArr);
                    v4 = v4.sub(v0);

                    crossNorm(v1, v2, v12);
                    crossNorm(v2, v3, v23);
                    crossNorm(v3, v4, v34);
                    crossNorm(v4, v1, v41);

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = 0;
                    normal.y = 1;
                    normal.z = 0;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return ListUtils.floatListToArray(normals);
    }

    private void assignVertexPosition(final Vector3f vec,
                                      final int row,
                                      final int col,
                                      final int width,
                                      final float[] posArr) {
        final int idx = row * width * 3 + col * 3;
        vec.x = posArr[idx];
        vec.y = posArr[idx + 1];
        vec.z = posArr[idx + 2];
    }

    private void crossNorm(final Vector3f a,
                           final Vector3f b,
                           final Vector3f c) {
        a.cross(b, c);
        c.normalize();
    }

    private float getHeight(final int x,
                            final int z,
                            final int width,
                            final ByteBuffer buffer) {
        final byte r = buffer.get(x * 4 + z * 4 * width);
        final byte g = buffer.get(x * 4 + 1 + z * 4 * width);
        final byte b = buffer.get(x * 4 + 2 + z * 4 * width);
        final byte a = buffer.get(x * 4 + 3 + z * 4 * width);
        final int argb = ((0xFF & a) << 24)
                | ((0xFF & r) << 16)
                | ((0xFF & g) << 8)
                | (0xFF & b);
        return this.minY + Math.abs(this.maxY - this.minY) * ((float) argb / (float) MAX_COLOUR);
    }
}