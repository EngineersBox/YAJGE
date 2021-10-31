package com.engineersbox.yajge.util;

public class Timer {

    private static final double NANOS_IN_SECONDS = 1_000_000_000.0;
    private double lastLoopTime;
    
    public void init() {
        this.lastLoopTime = getTime();
    }

    public double getTime() {
        return System.nanoTime() / NANOS_IN_SECONDS;
    }

    public float getElapsedTime() {
        final double time = getTime();
        final float elapsedTime = (float) (time - this.lastLoopTime);
        this.lastLoopTime = time;
        return elapsedTime;
    }

    public double getLastLoopTime() {
        return this.lastLoopTime;
    }
}
