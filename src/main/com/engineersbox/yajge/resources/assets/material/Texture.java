package com.engineersbox.yajge.resources.assets.material;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    private final int id;
    private final int width;
    private final int height;
    private int rows = 1;
    private int cols = 1;

    public Texture(final int width, final int height, final int pixelFormat)  {
        this.id = glGenTextures();
        this.width = width;
        this.height = height;
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_DEPTH_COMPONENT,
                this.width,
                this.height,
                0,
                pixelFormat,
                GL_FLOAT,
                (ByteBuffer) null
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public Texture(final String fileName) {
        final ByteBuffer buf;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);

            buf = STBImage.stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new RuntimeException("Image file [" + fileName  + "] not loaded: " + STBImage.stbi_failure_reason());
            }

            this.width = w.get();
            this.height = h.get();
        }
        this.id = createTexture(buf);
        STBImage.stbi_image_free(buf);
    }

    public Texture(final String fileName, final int cols, final int rows)  {
        this(fileName);
        this.cols = cols;
        this.rows = rows;
    }

    public Texture(final ByteBuffer imageBuffer) {
        final ByteBuffer buf;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);

            buf = STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buf == null) {
                throw new RuntimeException("Image file not loaded: " + STBImage.stbi_failure_reason());
            }

            this.width = w.get();
            this.height = h.get();
        }
        this.id = createTexture(buf);
        STBImage.stbi_image_free(buf);
    }

    private int createTexture(final ByteBuffer imageBuffer) {
        final int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                this.width,
                this.height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                imageBuffer
        );
        glGenerateMipmap(GL_TEXTURE_2D);
        return textureId;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    public int getId() {
        return this.id;
    }

    public void cleanup() {
        glDeleteTextures(this.id);
    }
}
