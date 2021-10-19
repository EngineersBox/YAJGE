package com.engineersbox.yajge.rendering.primitive;

import com.engineersbox.yajge.rendering.resources.materials.Material;
import com.engineersbox.yajge.rendering.resources.materials.Texture;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {

    private static final Vector3f DEFAULT_COLOUR = new Vector3f(1.0f, 1.0f, 1.0f);

    private final int vaoId;
    private final List<Integer> vboIdList;
    private final int vertexCount;
    private Material material;

    public Mesh(final float[] positions,
                final float[] textCoords,
                final float[] normals,
                final int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer texCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            this.vertexCount = indices.length;
            this.vboIdList = new ArrayList<>();
            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);

            posBuffer = allocateFloatBuffer(0, 3, positions);
            texCoordsBuffer = allocateFloatBuffer(1, 2, textCoords);
            vecNormalsBuffer = allocateFloatBuffer(2, 3, normals);
            indicesBuffer = allocateIndexBuffer(indices);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            freeAll(posBuffer, texCoordsBuffer, vecNormalsBuffer, indicesBuffer);
        }
    }

    private void freeAll(final java.nio.Buffer ...buffers) {
        for (final java.nio.Buffer bufferPtr : buffers) {
            if (bufferPtr != null) {
                MemoryUtil.memFree(bufferPtr);
            }
        }
    }

    private FloatBuffer allocateFloatBuffer(final int index,
                                            final int size,
                                            final float[] values) {
        final int vboId = glGenBuffers();
        this.vboIdList.add(vboId);
        final FloatBuffer buffer = MemoryUtil.memAllocFloat(values.length);
        buffer.put(values).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
        return buffer;
    }

    private IntBuffer allocateIndexBuffer(final int[] indices) {
        final int vboId = glGenBuffers();
        this.vboIdList.add(vboId);
        final IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        return indicesBuffer;
    }

    public void render() {
        final Texture texture = this.material.getTexture();
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        glBindVertexArray(getVaoId());
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (final int vboId : this.vboIdList) {
            glDeleteBuffers(vboId);
        }
        final Texture texture = this.material.getTexture();
        if (texture != null) {
            texture.cleanup();
        }
        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoId);
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getVaoId() {
        return this.vaoId;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }
}