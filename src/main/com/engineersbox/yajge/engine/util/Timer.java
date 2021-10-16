package com.engineersbox.yajge.engine.util;

public class Timer {

    private static final double NANOSECOND_IN_SECOND = 1_000_000_000.0;
    private double lastLoopTimeSeconds;

    public void init() {
        this.lastLoopTimeSeconds = getTime();
    }

    public double getTime() {
        return System.nanoTime() / NANOSECOND_IN_SECOND;
    }

    public float getElapsedTime() {
        final double currentTimeSeconds = getTime();
        final float elapsedTime = (float) (currentTimeSeconds - this.lastLoopTimeSeconds);
        this.lastLoopTimeSeconds = currentTimeSeconds;
        return elapsedTime;
    }

    public double getLastLoopTime() {
        return this.lastLoopTimeSeconds;
    }
}
