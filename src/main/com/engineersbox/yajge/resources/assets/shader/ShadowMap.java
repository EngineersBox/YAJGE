package com.engineersbox.yajge.resources.assets.shader;

import com.engineersbox.yajge.resources.assets.material.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {

    public static final int SHADOW_MAP_WIDTH = 1024;
    public static final int SHADOW_MAP_HEIGHT = 1024;

    private final int depthMapFBO;
    private final Texture depthMap;

    public ShadowMap() {
        this.depthMapFBO = glGenFramebuffers();

        this.depthMap = new Texture(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);

        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthMap.getId(), 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FrameBuffer");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Texture getDepthMapTexture() {
        return this.depthMap;
    }

    public int getDepthMapFBO() {
        return this.depthMapFBO;
    }

    public void cleanup() {
        glDeleteFramebuffers(this.depthMapFBO);
        this.depthMap.cleanup();
    }
}
