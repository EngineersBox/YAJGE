package com.engineersbox.yajge.engine.core;

public interface EngineLogic {

    void init(final Window window) throws Exception;
    void input(final Window window);
    void update(final float interval);
    void render(final Window window);
    void cleanup();
}
