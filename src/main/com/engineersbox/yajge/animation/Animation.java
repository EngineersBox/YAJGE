package com.engineersbox.yajge.animation;

import java.util.List;

public class Animation {

    private int currentFrame;
    private final List<AnimatedFrame> frames;
    private final String name;
    private final double duration;

    public Animation(final String name,
                     final List<AnimatedFrame> frames,
                     final double duration) {
        this.name = name;
        this.frames = frames;
        this.currentFrame = 0;
        this.duration = duration;
    }

    public AnimatedFrame getCurrentFrame() {
        return this.frames.get(this.currentFrame);
    }

    public double getDuration() {
        return this.duration;        
    }
    
    public List<AnimatedFrame> getFrames() {
        return this.frames;
    }

    public String getName() {
        return this.name;
    }

    public AnimatedFrame getNextFrame() {
        nextFrame();
        return this.frames.get(this.currentFrame);
    }

    public void nextFrame() {
        this.currentFrame = Math.min(Math.max(this.currentFrame + 1, 0), this.frames.size() - 1);
    }

}
