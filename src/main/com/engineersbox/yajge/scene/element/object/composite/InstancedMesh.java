package com.engineersbox.yajge.scene.element.object.composite;

import com.engineersbox.yajge.rendering.view.Transform;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.util.ArrayUtils;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
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
    private static final int INSTANCE_SIZE_BYTES = MAT4F_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES * 2;
    private static final int INSTANCE_SIZE_FLOATS = MAT4F_SIZE_FLOATS * 2 + 2;

    private final int chunkSizeInstances;
    private final int instanceDataVBO;
    private final FloatBuffer instanceDataBuffer;

    public InstancedMesh(final float[] positions,
                         final float[] textCoords,
                         final float[] normals,
                         final int[] indices,
                         final int chunkSizeInstances) {
        super(
                positions,
                textCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0.0f)
        );
        this.chunkSizeInstances = chunkSizeInstances;
        glBindVertexArray(this.vaoId);

        this.instanceDataVBO = glGenBuffers();
        this.vboIdList.add(this.instanceDataVBO);
        this.instanceDataBuffer = MemoryUtil.memAllocFloat(chunkSizeInstances * INSTANCE_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, this.instanceDataVBO);

        int start = 5;
        int strideStart = 0;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);
        glEnableVertexAttribArray(start);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderListInstanced(final List<SceneElement> sceneElements,
                                    final Transform transform,
                                    final Matrix4f viewMatrix,
                                    final Matrix4f lightViewMatrix) {
        renderListInstanced(
                sceneElements,
                false,
                transform,
                viewMatrix,
                lightViewMatrix
        );
    }

    public void renderListInstanced(final List<SceneElement> sceneElements,
                                    final boolean billBoard,
                                    final Transform transform,
                                    final Matrix4f viewMatrix,
                                    final Matrix4f lightViewMatrix) {
        super.startRender();
        for (int i = 0; i < sceneElements.size(); i += this.chunkSizeInstances) {
            final int end = Math.min(sceneElements.size(), i + this.chunkSizeInstances);
            final List<SceneElement> subList = sceneElements.subList(i, end);
            renderChunkInstanced(subList, billBoard, transform, viewMatrix, lightViewMatrix);
        }
        super.endRender();
    }

    private void renderChunkInstanced(final List<SceneElement> sceneElements,
                                      final boolean billBoard,
                                      final Transform transform,
                                      final Matrix4f viewMatrix,
                                      final Matrix4f lightViewMatrix) {
        this.instanceDataBuffer.clear();
        final Texture texture = super.getMaterial().getTexture();
        for (int i = 0; i < sceneElements.size(); i++) {
            final SceneElement sceneElement = sceneElements.get(i);
            final Matrix4f modelMatrix = transform.buildModelMatrix(sceneElement);
            if (viewMatrix != null) {
                if (billBoard) {
                    viewMatrix.transpose3x3(modelMatrix);
                }
                final Matrix4f viewModelMatrix = transform.buildViewModelMatrix(modelMatrix, viewMatrix);
                if (billBoard) {
                    viewModelMatrix.scale(sceneElement.getScale());
                }
                viewModelMatrix.get(INSTANCE_SIZE_FLOATS * i, this.instanceDataBuffer);
            }
            if (lightViewMatrix != null) {
                final Matrix4f modelLightViewMatrix = transform.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                modelLightViewMatrix.get(INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS, this.instanceDataBuffer);
            }
            if (texture != null) {
                final int col = sceneElement.getTextPos() % texture.getCols();
                final int row = sceneElement.getTextPos() / texture.getCols();
                final float textXOffset = (float) col / texture.getCols();
                final float textYOffset = (float) row / texture.getRows();
                final int buffPos = INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS * 2;
                this.instanceDataBuffer.put(buffPos, textXOffset);
                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.instanceDataVBO);
        glBufferData(GL_ARRAY_BUFFER, this.instanceDataBuffer, GL_DYNAMIC_DRAW);
        glDrawElementsInstanced(
                GL_TRIANGLES,
                super.getVertexCount(),
                GL_UNSIGNED_INT,
                0,
                sceneElements.size()
        );
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
