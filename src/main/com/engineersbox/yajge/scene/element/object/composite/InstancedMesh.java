package com.engineersbox.yajge.scene.element.object.composite;

import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.util.AllocUtils;
import com.engineersbox.yajge.util.ArrayUtils;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedMesh extends Mesh {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;
    private static final int MAT4F_SIZE_FLOATS = 4 * 4;
    private static final int MAT4F_SIZE_BYTES = MAT4F_SIZE_FLOATS * FLOAT_SIZE_BYTES;
    private static final int INSTANCE_SIZE_BYTES = MAT4F_SIZE_BYTES + FLOAT_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES;
    private static final int INSTANCE_SIZE_FLOATS = MAT4F_SIZE_FLOATS + 3;

    private final int numInstances;
    private final int instanceDataVBO;
    private FloatBuffer instanceDataBuffer;

    public InstancedMesh(final float[] positions,
                         final float[] texCoords,
                         final float[] normals,
                         final int[] indices,
                         final int numInstances) {
        super(
                positions,
                texCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0f)
        );
        this.numInstances = numInstances;
        glBindVertexArray(this.vaoId);
        this.instanceDataVBO = glGenBuffers();
        this.vboIdList.add(this.instanceDataVBO);
        this.instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, this.instanceDataVBO);
        int start = 5;
        int strideStart = 0;

        // Model matrix
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        // Texture offsets
        glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);
        glEnableVertexAttribArray(start);
        strideStart += FLOAT_SIZE_BYTES * 2;
        start++;

        // Selected or Scaling (for particles)
        glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);
        glEnableVertexAttribArray(start);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        AllocUtils.freeAll(this.instanceDataBuffer);
        this.instanceDataBuffer = null;
    }

    public void renderInstanced(final List<SceneElement> sceneElements,
                                final Transform transform,
                                final Matrix4f viewMatrix) {
        renderInstanced(sceneElements, false, transform, viewMatrix);
    }

    public void renderInstanced(final List<SceneElement> sceneElements,
                                final boolean billBoard,
                                final Transform transform,
                                final Matrix4f viewMatrix) {
        startRender();
        final int chunkSize = this.numInstances;
        final int length = sceneElements.size();
        for (int i = 0; i < length; i += chunkSize) {
            final int end = Math.min(length, i + chunkSize);
            renderChunkInstanced(
                    sceneElements.subList(i, end),
                    billBoard,
                    transform,
                    viewMatrix
            );
        }
        endRender();
    }

    private void renderChunkInstanced(final List<SceneElement> sceneElements,
                                      final boolean billBoard,
                                      final Transform transform,
                                      final Matrix4f viewMatrix) {
        this.instanceDataBuffer.clear();
        final Texture texture = getMaterial().getTexture();
        for (int i = 0; i < sceneElements.size(); i++) {
            final SceneElement sceneElement = sceneElements.get(i);
            final Matrix4f modelMatrix = transform.buildModelMatrix(sceneElement);
            if (viewMatrix != null && billBoard) {
                viewMatrix.transpose3x3(modelMatrix);
            }
            modelMatrix.get(INSTANCE_SIZE_FLOATS * i, this.instanceDataBuffer);
            if (texture != null) {
                final int col = sceneElement.getTexPos() % texture.getCols();
                final int row = sceneElement.getTexPos() / texture.getCols();
                final float textXOffset = (float) col / texture.getCols();
                final float textYOffset = (float) row / texture.getRows();
                final int buffPos = INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS;
                this.instanceDataBuffer.put(buffPos, textXOffset);
                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
            }

            final int buffPos = INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS + 2;
            final float selectedScale = sceneElement.isSelected() ? 1 : 0;
            this.instanceDataBuffer.put(
                    buffPos,
                    billBoard ? sceneElement.getScale() : selectedScale
            );
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.instanceDataVBO);
        glBufferData(GL_ARRAY_BUFFER, this.instanceDataBuffer, GL_DYNAMIC_READ);
        glDrawElementsInstanced(
                GL_TRIANGLES,
                getVertexCount(),
                GL_UNSIGNED_INT,
                0,
                sceneElements.size()
        );
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
