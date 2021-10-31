package com.engineersbox.yajge.scene.element.object.composite;

import com.engineersbox.yajge.rendering.view.Transform;
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
    private static final int VEC4F_SIZE_BYTES = 4 * 4;
    private static final int MAT4_SIZE_BYTES = 4 * VEC4F_SIZE_BYTES;
    private static final int MAT4_SIZE_FLOATS = 4 * 4;

    private final int numInstances;
    private final int modelViewVBO;
    private final int modelLightViewVBO;
    private FloatBuffer modelViewBuffer;
    private FloatBuffer modelLightViewBuffer;

    public InstancedMesh(final float[] positions,
                         final float[] textCoords,
                         final float[] normals,
                         final int[] indices,
                         final int numInstances) {
        super(
                positions,
                textCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0.0f)
        );
        this.numInstances = numInstances;
        glBindVertexArray(super.vaoId);

        this.modelViewVBO = glGenBuffers();
        super.vboIdList.add(this.modelViewVBO);
        this.modelViewBuffer = MemoryUtil.memAllocFloat(numInstances * InstancedMesh.MAT4_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, this.modelViewVBO);
        int start = 5;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, InstancedMesh.MAT4_SIZE_BYTES, i * (long) InstancedMesh.VEC4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        this.modelLightViewVBO = glGenBuffers();
        this.vboIdList.add(this.modelLightViewVBO);
        this.modelLightViewBuffer = MemoryUtil.memAllocFloat(numInstances * InstancedMesh.MAT4_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, this.modelLightViewVBO);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, InstancedMesh.MAT4_SIZE_BYTES, i * (long) InstancedMesh.VEC4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (this.modelViewBuffer != null) {
            MemoryUtil.memFree(this.modelViewBuffer);
            this.modelViewBuffer = null;
        }
        if (this.modelLightViewBuffer != null) {
            MemoryUtil.memFree(this.modelLightViewBuffer);
            this.modelLightViewBuffer = null;
        }
    }

    public void renderListInstanced(final List<SceneElement> sceneElements,
                                    final boolean depthMap,
                                    final Transform transform,
                                    final Matrix4f viewMatrix,
                                    final Matrix4f lightViewMatrix) {
        startRender();

        final int chunkSize = this.numInstances;
        for (int i = 0; i < sceneElements.size(); i += chunkSize) {
            final int end = Math.min(sceneElements.size(), i + chunkSize);
            final List<SceneElement> chunkOfSceneElements = sceneElements.subList(i, end);
            renderChunkInstanced(
                    chunkOfSceneElements,
                    depthMap,
                    transform,
                    viewMatrix,
                    lightViewMatrix
            );
        }

        endRender();
    }

    private void renderChunkInstanced(final List<SceneElement> sceneElements,
                                      final boolean depthMap,
                                      final Transform transform,
                                      final Matrix4f viewMatrix,
                                      final Matrix4f lightViewMatrix) {
        this.modelViewBuffer.clear();
        this.modelLightViewBuffer.clear();

        for (int i = 0; i < sceneElements.size(); i++) {
            final Matrix4f modelMatrix = transform.buildModelMatrix(sceneElements.get(i));
            if (!depthMap) {
                final Matrix4f viewModelMatrix = transform.buildViewModelMatrix(modelMatrix, viewMatrix);
                viewModelMatrix.get(InstancedMesh.MAT4_SIZE_FLOATS * i, this.modelViewBuffer);
            }
            final Matrix4f modelLightViewMatrix = transform.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
            modelLightViewMatrix.get(InstancedMesh.MAT4_SIZE_FLOATS * i, this.modelLightViewBuffer);
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.modelViewVBO);
        glBufferData(GL_ARRAY_BUFFER, this.modelViewBuffer, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, this.modelLightViewVBO);
        glBufferData(GL_ARRAY_BUFFER, this.modelLightViewBuffer, GL_DYNAMIC_DRAW);

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
