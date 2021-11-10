package com.engineersbox.yajge.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private final int sourceId;

    public SoundSource(final boolean loop, final boolean relative) {
        this.sourceId = alGenSources();
        if (loop) {
            alSourcei(this.sourceId, AL_LOOPING, AL_TRUE);
        }
        if (relative) {
            alSourcei(this.sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }
    }

    public void setBuffer(final int bufferId) {
        stop();
        alSourcei(this.sourceId, AL_BUFFER, bufferId);
    }

    public void setPosition(final Vector3f position) {
        alSource3f(this.sourceId, AL_POSITION, position.x, position.y, position.z);
    }

    public void setSpeed(final Vector3f speed) {
        alSource3f(this.sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    public void setGain(final float gain) {
        alSourcef(this.sourceId, AL_GAIN, gain);
    }

    public void setProperty(final int param, final float value) {
        alSourcef(this.sourceId, param, value);
    }
    
    public void play() {
        alSourcePlay(this.sourceId);
    }

    public boolean isPlaying() {
        return alGetSourcei(this.sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(this.sourceId);
    }

    public void stop() {
        alSourceStop(this.sourceId);
    }

    public void cleanup() {
        stop();
        alDeleteSources(this.sourceId);
    }
}
