package com.engineersbox.yajge.rendering.scene.shadow;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;

public class TextureArray {

    private final int[] ids;
    private final int width;
    private final int height;

    public TextureArray(final int numTextures,
                        final int width,
                        final int height,
                        final int pixelFormat)  {
        this.ids = new int[numTextures];
        glGenTextures(this.ids);
        this.width = width;
        this.height = height;

        Arrays.stream(this.ids).forEach((final int id) -> {
            glBindTexture(GL_TEXTURE_2D, id);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        });
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int[] getIds() {
        return this.ids;
    }

    public void cleanup() {
        Arrays.stream(this.ids).forEach(GL11::glDeleteTextures);
    }
}
