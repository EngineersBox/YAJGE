package com.engineersbox.yajge.engine.core;

import com.engineersbox.yajge.input.MouseInput;

public interface EngineLogic {

    void init(final Window window);
    void input(final Window window, final MouseInput mouseInput);
    void update(final float interval, final MouseInput mouseInput);
    void render(final Window window);
    void cleanup();
}
