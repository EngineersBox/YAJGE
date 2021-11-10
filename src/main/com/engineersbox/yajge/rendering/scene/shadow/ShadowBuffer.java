package com.engineersbox.yajge.rendering.scene.shadow;

import static org.lwjgl.opengl.GL30.*;

public class ShadowBuffer {

    public static final int SHADOW_MAP_WIDTH = (int)Math.pow(65, 2);
    public static final int SHADOW_MAP_HEIGHT = SHADOW_MAP_WIDTH;

    private final int depthMapFBO;
    private final TextureArray depthMap;

    public ShadowBuffer()  {
        this.depthMapFBO = glGenFramebuffers();

        this.depthMap = new TextureArray(ShadowRenderer.NUM_CASCADES, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);

        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthMap.getIds()[0], 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FrameBuffer");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public TextureArray getDepthMapTexture() {
        return this.depthMap;
    }

    public int getDepthMapFBO() {
        return this.depthMapFBO;
    }

    public void bindTextures(final int start) {
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            glActiveTexture(start + i);
            glBindTexture(GL_TEXTURE_2D, this.depthMap.getIds()[i]);
        }
    }
    
    public void cleanup() {
        glDeleteFramebuffers(this.depthMapFBO);
        this.depthMap.cleanup();
    }
    
}
