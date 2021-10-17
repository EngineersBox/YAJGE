package com.engineersbox.yajge.rendering.resources.textures;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final int id;

    public Texture(final String fileName) throws Exception {
        this(loadTexture(fileName));
    }

    public Texture(final int id) {
        this.id = id;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    public int getId() {
        return this.id;
    }

    private static int loadTexture(final String fileName) throws Exception {
        final int width;
        final int height;
        final ByteBuffer imageBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);
            imageBuffer = stbi_load(fileName, w, h, channels, 4);
            if (imageBuffer == null) {
                throw new RuntimeException(String.format(
                        "Could not load image file %s: %s",
                        fileName,
                        stbi_failure_reason()
                )); // TODO: Implement an exception for this
            }
            width = w.get();
            height = h.get();
        }
        return createAndBindTexture(width, height, imageBuffer);
    }

    private static int createAndBindTexture(final int width,
                                            final int height,
                                            final ByteBuffer imageBuffer) {
        final int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                imageBuffer
        );
        glGenerateMipmap(GL_TEXTURE_2D);
        stbi_image_free(imageBuffer);
        return textureId;
    }

    public void cleanup() {
        glDeleteTextures(this.id);
    }
}