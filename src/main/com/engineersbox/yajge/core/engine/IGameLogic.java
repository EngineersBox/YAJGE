package com.engineersbox.yajge.core.engine;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.input.MouseInput;

public interface IGameLogic {

    void init(final Window window) ;
    void input(final Window window, final MouseInput mouseInput);
    void update(final float interval, final MouseInput mouseInput, final Window window);
    void render(final Window window);
    void cleanup();
}