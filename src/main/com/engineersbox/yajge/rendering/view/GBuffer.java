package com.engineersbox.yajge.rendering.view;

import com.engineersbox.yajge.core.window.Window;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL30.*;

public class GBuffer {

    private static final int TOTAL_TEXTURES = 6;

    private final int gBufferId;
    private final int[] textureIds;
    private final int width;
    private final int height;

    public GBuffer(final Window window)  {
        this.gBufferId = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.gBufferId);

        this.textureIds = new int[TOTAL_TEXTURES];
        glGenTextures(this.textureIds);

        this.width = window.getWidth();
        this.height = window.getHeight();

        for(int i = 0; i < TOTAL_TEXTURES; i++) {
            glBindTexture(GL_TEXTURE_2D, this.textureIds[i]);
            final int attachmentType;
            // Depth component
            if (i == TOTAL_TEXTURES - 1) {
                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_DEPTH_COMPONENT32F,
                        this.width,
                        this.height,
                        0,
                        GL_DEPTH_COMPONENT,
                        GL_FLOAT,
                        (ByteBuffer) null
                );
                attachmentType = GL_DEPTH_ATTACHMENT;
            } else {
                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGB32F,
                        this.width,
                        this.height,
                        0,
                        GL_RGB,
                        GL_FLOAT,
                        (ByteBuffer) null
                );
                attachmentType = GL_COLOR_ATTACHMENT0 + i;
            }
            // Sampling
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    attachmentType,
                    GL_TEXTURE_2D,
                    this.textureIds[i],
                    0
            );
        }

        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer intBuff = stack.mallocInt(TOTAL_TEXTURES);
            Stream.of(
                    GL_COLOR_ATTACHMENT0,
                    GL_COLOR_ATTACHMENT1,
                    GL_COLOR_ATTACHMENT2,
                    GL_COLOR_ATTACHMENT3,
                    GL_COLOR_ATTACHMENT4,
                    GL_COLOR_ATTACHMENT5
            ).forEach(intBuff::put);
            intBuff.flip();
            glDrawBuffers(intBuff);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getGBufferId() {
        return this.gBufferId;
    }

    public int[] getTextureIds() {
        return this.textureIds;
    }

    public int getPositionTexture() {
        return this.textureIds[0];
    }

    public int getDepthTexture() {
        return this.textureIds[TOTAL_TEXTURES-1];
    }

    public void cleanUp() {
        glDeleteFramebuffers(this.gBufferId);

        if (this.textureIds != null) {
            Arrays.stream(this.textureIds, 0, TOTAL_TEXTURES).forEach(GL11::glDeleteTextures);
        }
    }
}
