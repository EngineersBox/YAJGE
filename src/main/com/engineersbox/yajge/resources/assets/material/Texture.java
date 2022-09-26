package com.engineersbox.yajge.resources.assets.material;

import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.resources.loader.ResourceLoader;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {

    private final int id;
    private final int width;
    private final int height;
    private int rows = 1;
    private int cols = 1;

    public Texture(final int width,
                   final int height,
                   final int pixelFormat)  {
        this.id = glGenTextures();
        this.width = width;
        this.height = height;
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexImage2D(
                GL_TEXTURE_2D,
                ConfigHandler.CONFIG.render.texture.lodBias,
                GL_DEPTH_COMPONENT,
                this.width,
                this.height,
                0,
                pixelFormat,
                GL_FLOAT,
                (ByteBuffer) null
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, getMipmapType());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, getMipmapType());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    private static int getMipmapType() {
        return switch (ConfigHandler.CONFIG.render.texture.mipmaps) {
            case NONE -> GL_NEAREST_MIPMAP_NEAREST;
            case BILINEAR -> GL_LINEAR_MIPMAP_NEAREST;
            case TRILINEAR -> GL_LINEAR_MIPMAP_LINEAR;
        };
    }

    public Texture(final String fileName,
                   final int cols,
                   final int rows)  {
        this(fileName);
        this.cols = cols;
        this.rows = rows;
    }

    public Texture(final String fileName)  {
        this(ResourceLoader.ioResourceToByteBuffer(fileName));
    }

    public Texture(final ByteBuffer imageData) {
        try (final MemoryStack stack = stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer avChannels = stack.mallocInt(1);

            final ByteBuffer decodedImage = STBImage.stbi_load_from_memory(imageData, w, h, avChannels, 4);
            if (decodedImage == null) {
                throw new RuntimeException("Could not load image from memory");
            }

            this.width = w.get();
            this.height = h.get();

            this.id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.id);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, getMipmapType());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, getMipmapType());
            glTexImage2D(
                    GL_TEXTURE_2D,
                    ConfigHandler.CONFIG.render.texture.lodBias,
                    GL_RGBA,
                    this.width,
                    this.height,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    decodedImage
            );
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(decodedImage);
        }
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
