package com.engineersbox.yajge.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundListener {

    public SoundListener() {
        this(new Vector3f());
    }

    public SoundListener(final Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }
    
    public void setSpeed(final Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    public void setPosition(final Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }
    
    public void setOrientation(final Vector3f at, final Vector3f up) {
        alListenerfv(
                AL_ORIENTATION,
                new float[]{
                        at.x,
                        at.y,
                        at.z,
                        up.x,
                        up.y,
                        up.z
                }
        );
    }
}
