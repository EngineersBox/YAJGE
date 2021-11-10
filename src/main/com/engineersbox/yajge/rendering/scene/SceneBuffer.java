package com.engineersbox.yajge.rendering.scene;

import com.engineersbox.yajge.core.window.Window;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class SceneBuffer {

    private final int bufferId;
    private final int textureId;

    public SceneBuffer(final Window window)  {
        this.bufferId = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.bufferId);

        final int[] textureIds = new int[1];
        glGenTextures(textureIds);
        this.textureId = textureIds[0];
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGB32F,
                window.getWidth(),
                window.getHeight(),
                0,
                GL_RGB,
                GL_FLOAT,
                (ByteBuffer) null
        );

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                this.textureId,
                0
        );

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getBufferId() {
        return this.bufferId;
    }

    public int getTextureId() {
        return this.textureId;
    }

    public void cleanup() {
        glDeleteFramebuffers(this.bufferId);

        glDeleteTextures(this.textureId);
    }
}
