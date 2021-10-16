package com.engineersbox.yajge.engine.core;

public interface IGameLogic {

    void init() throws Exception;
    void input(final Window window);
    void update(final float interval);
    void render(final Window window);
    void cleanup();
}