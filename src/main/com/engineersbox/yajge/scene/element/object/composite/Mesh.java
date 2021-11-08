package com.engineersbox.yajge.scene.element.object.composite;

import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.util.AllocUtils;
import com.engineersbox.yajge.util.ArrayUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;

    protected final int vaoId;
    protected final List<Integer> vboIdList;
    private final int vertexCount;
    private Material material;
    private float boundingRadius;

    public Mesh(final float[] positions, final float[] texCoords, final float[] normals, final int[] indices) {
        this(
                positions,
                texCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0.0f)
        );
    }

    public Mesh(final float[] positions,
                final float[] texCoords,
                final float[] normals,
                final int[] indices,
                final int[] jointIndices,
                final float[] weights) {
        FloatBuffer posBuffer = null;
        FloatBuffer texCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        FloatBuffer weightsBuffer = null;
        IntBuffer jointIndicesBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            calculateBoundingRadius(positions);

            this.vertexCount = indices.length;
            this.vboIdList = new ArrayList<>();
            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);

            posBuffer = allocateFloatBuffer(0, 3, positions);
            texCoordsBuffer = allocateFloatBuffer(1, 2, texCoords);

            final int vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            if (vecNormalsBuffer.capacity() > 0) {
                vecNormalsBuffer.put(normals).flip();
            } else {
                vecNormalsBuffer = MemoryUtil.memAllocFloat(positions.length);
            }
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            weightsBuffer = allocateFloatBuffer(3, 4, weights);
            jointIndicesBuffer = allocateIntBuffer(4, 4, jointIndices);
            indicesBuffer = allocateIndexBuffer(indices);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            AllocUtils.freeAll(
                    posBuffer,
                    texCoordsBuffer,
                    vecNormalsBuffer,
                    indicesBuffer,
                    weightsBuffer,
                    jointIndicesBuffer
            );
        }
    }

    private void calculateBoundingRadius(final float[] positions) {
        this.boundingRadius = 0;
        for (final float pos : positions) {
            this.boundingRadius = Math.max(Math.abs(pos), this.boundingRadius);
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

    private IntBuffer allocateIntBuffer(final int index,
                                        final int size,
                                        final int[] values) {
        final IntBuffer intBuffer = allocateIndexBuffer(values);
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
        return intBuffer;
    }

    protected void startRender() {
        final Texture texture = this.material != null ? this.material.getTexture() : null;
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        final Texture normalMap = this.material != null ? this.material.getNormalMap() : null;
        if (normalMap != null) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
        }

        glBindVertexArray(getVaoId());
    }

    protected void endRender() {
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void renderList(final List<SceneElement> sceneElements,
                           final Consumer<SceneElement> consumer) {
        startRender();
        sceneElements.stream()
                .filter(SceneElement::isInsideFrustum)
                .forEach((final SceneElement sceneElement) -> {
                    consumer.accept(sceneElement);
                    glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
                });
        endRender();
    }

    public void render() {
        startRender();
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        endRender();
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        this.vboIdList.forEach(GL15::glDeleteBuffers);
        final Texture texture = this.material.getTexture();
        if (texture != null) {
            texture.cleanup();
        }
        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoId);
    }

    public void deleteBuffers() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        this.vboIdList.forEach(GL15::glDeleteBuffers);

        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoId);
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(final Material material) {
        this.material = material;
    }

    public int getVaoId() {
        return this.vaoId;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    public void setBoundingRadius(final float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }
}
