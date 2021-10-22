package com.engineersbox.yajge.engine;

import com.engineersbox.yajge.engine.core.EngineLogic;
import com.engineersbox.yajge.engine.core.Window;
import com.engineersbox.yajge.engine.util.Timer;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.logging.LoggerCompat;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Engine implements Runnable {

//    public static final int TARGET_FPS = 75;
//    public static final int TARGET_UPS = 30;

    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Window window;
    private final Timer timer;
    private final EngineLogic gameLogic;
    private final MouseInput mouseInput;
    private boolean running = false;

    public Engine(final String windowTitle,
                  final int width,
                  final int height,
                  final boolean vSync,
                  final EngineLogic gameLogic) {
        LoggerCompat.registerGLFWErrorLogger(LOGGER, Level.ERROR);
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
            e.printStackTrace(LoggerCompat.asPrintStream(LOGGER, Level.ERROR));
        }
    }

    protected void init() {
        this.window.init();
        this.timer.init();
        this.mouseInput.init(this.window);
        this.gameLogic.init(this.window);
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        final float interval = 1f / ConfigHandler.CONFIG.video.ups;
        this.running = true;
        while (this.running && !this.window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;
            input();
            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }
            render();
            if (!this.window.isVSyncEnabled()) {
                sync();
            }
        }
        this.running = false;
    }

    private void sync() {
        final float loopSlot = 1f / ConfigHandler.CONFIG.video.fps;
        final double endTime = this.timer.getLastLoopTime() + loopSlot;
        try {
            Thread.sleep((long) (endTime - this.timer.getTime()));
        } catch (final InterruptedException e) {
            e.printStackTrace(LoggerCompat.asPrintStream(LOGGER, Level.ERROR));
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

    protected boolean isRunning() {
        return this.running;
    }
}
