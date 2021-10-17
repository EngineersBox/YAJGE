package com.engineersbox.yajge.rendering.primitive;

import com.engineersbox.yajge.rendering.resources.textures.Texture;
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

    private final int vaoId;
    private final List<Integer> vboIdList;
    private final int vertexCount;
    private final Texture texture;

    public Mesh(final float[] positions,
                final float[] texCoords,
                final int[] indices,
                final Texture texture) {
        FloatBuffer posBuffer = null;
        FloatBuffer texCoordsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            this.texture = texture;
            this.vertexCount = indices.length;
            this.vboIdList = new ArrayList<>();
            this.vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);
            posBuffer = createPosBuffer(positions);
            texCoordsBuffer = createTexCoordsBuffer(texCoords);
            indicesBuffer = createIndexBuffer(indices);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (texCoordsBuffer != null) {
                MemoryUtil.memFree(texCoordsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    private FloatBuffer createPosBuffer(final float[] positions) {
        final int vboId = glGenBuffers();
        this.vboIdList.add(vboId);
        final FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        return posBuffer;
    }

    private FloatBuffer createTexCoordsBuffer(final float[] texCoords) {
        final int vboId = glGenBuffers();
        this.vboIdList.add(vboId);
        final FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texCoordsBuffer.put(texCoords).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        return texCoordsBuffer;
    }

    private IntBuffer createIndexBuffer(final int[] indices) {
        final int vboId = glGenBuffers();
        this.vboIdList.add(vboId);
        final IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        return indicesBuffer;
    }

    public int getVaoId() {
        return this.vaoId;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.texture.getId());
        glBindVertexArray(getVaoId());
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (final int vboId : this.vboIdList) {
            glDeleteBuffers(vboId);
        }
        this.texture.cleanup();
        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoId);
    }
}