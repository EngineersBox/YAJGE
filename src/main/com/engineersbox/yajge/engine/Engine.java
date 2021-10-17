package com.engineersbox.yajge.engine;

import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.engine.util.Timer;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.logging.LoggerCompat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Engine implements Runnable {

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;

    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Window window;
    private final Timer timer;
    private final EngineLogic gameLogic;
    private final MouseInput mouseInput;

    public Engine(final String windowTitle,
                  final int width,
                  final int height,
                  final boolean vSync,
                  final EngineLogic gameLogic) throws Exception {
        LoggerCompat.registerGLFWLogger(LOGGER);
        this.window = new Window(windowTitle, width, height, vSync);
        this.gameLogic = gameLogic;
        this.mouseInput = new MouseInput();
        this.timer = new Timer();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (final Exception e) {
            LOGGER.error(e);
        }
    }

    protected void init() throws Exception {
        this.window.init();
        this.timer.init();
        this.mouseInput.init(this.window);
        this.gameLogic.init(this.window);
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        final float interval = 1f / TARGET_UPS;

        boolean running = true;
        while (running && !this.window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if (!this.window.isvSync()) {
                sync();
            }
        }
    }

    private void sync() {
        final float loopSlot = 1f / TARGET_FPS;
        final double endTime = this.timer.getLastLoopTime() + loopSlot;
        while (this.timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                LOGGER.error(e);
            }
        }
    }

    protected void input() {
        this.mouseInput.input(this.window);
        this.gameLogic.input(this.window, this.mouseInput);
    }

    protected void update(final float interval) {
        this.gameLogic.update(interval, this.mouseInput);
    }

    protected void render() {
        this.gameLogic.render(window);
        this.window.update();
    }
}
