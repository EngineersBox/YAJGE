package com.engineersbox.yajge.scene.element.animation;

import com.engineersbox.yajge.animation.AnimatedFrame;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.Matrix4f;

import java.util.List;

public class AnimatedSceneElement extends SceneElement {

    private int currentFrame;
    private List<AnimatedFrame> frames;
    private final List<Matrix4f> invJointMatrices;
    
    public AnimatedSceneElement(final Mesh[] meshes,
                                final List<AnimatedFrame> frames,
                                final List<Matrix4f> invJointMatrices) {
        super(meshes);
        this.frames = frames;
        this.invJointMatrices = invJointMatrices;
        this.currentFrame = 0;
    }

    public List<AnimatedFrame> getFrames() {
        return this.frames;
    }

    public void setFrames(final List<AnimatedFrame> frames) {
        this.frames = frames;
    }
    
    public AnimatedFrame getCurrentFrame() {
        return this.frames.get(this.currentFrame);
    }
    
    public AnimatedFrame getNextFrame() {
        return this.frames.get((this.currentFrame + 1) % this.frames.size());
    }

    public void nextFrame() {
        final int nextFrame = this.currentFrame + 1;
        if ( nextFrame > this.frames.size() - 1) {
            this.currentFrame = 0;
        } else {
            this.currentFrame = nextFrame;
        }
        this.currentFrame = (this.currentFrame + 1) % this.frames.size();
    }    

    public List<Matrix4f> getInvJointMatrices() {
        return this.invJointMatrices;
    }
}
