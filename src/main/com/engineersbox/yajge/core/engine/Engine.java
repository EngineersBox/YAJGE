package com.engineersbox.yajge.core.engine;

import com.engineersbox.yajge.core.window.Window;
import com.engineersbox.yajge.core.window.WindowOptions;
import com.engineersbox.yajge.input.MouseInput;
import com.engineersbox.yajge.logging.LoggerCompat;
import com.engineersbox.yajge.resources.config.io.ConfigHandler;
import com.engineersbox.yajge.util.Timer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Engine implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Window window;
    private final Timer timer;
    private final IGameLogic gameLogic;
    private final MouseInput mouseInput;
    private double lastFps;
    private int fps;
    private final String windowTitle;
    private boolean running = false;

    public Engine() {
        LoggerCompat.registerGLFWErrorLogger(LOGGER, Level.ERROR);
        this.window = null;
        this.timer = null;
        this.gameLogic = null;
        this.mouseInput = null;
        this.windowTitle = "YAJGE";
    }

    public Engine(final String windowTitle,
                  final IGameLogic gameLogic) {
        this(
                windowTitle,
                WindowOptions.createFromConfig(),
                gameLogic
        );
    }

    public Engine(final String windowTitle,
                  final WindowOptions opts,
                  final IGameLogic gameLogic) {
        this(
                windowTitle,
                0,
                0,
                opts,
                gameLogic
        );
    }

    public Engine(final String windowTitle,
                  final int width,
                  final int height,
                  final IGameLogic gameLogic) {
        this(
                windowTitle,
                width,
                height,
                WindowOptions.createFromConfig(),
                gameLogic
        );
    }

    public Engine(final String windowTitle,
                  final int width,
                  final int height,
                  final WindowOptions opts,
                  final IGameLogic gameLogic) {
        LoggerCompat.registerGLFWErrorLogger(LOGGER, Level.ERROR);
        this.window = new Window(
                windowTitle,
                width,
                height,
                ConfigHandler.CONFIG.video.vsync,
                opts
        );
        this.windowTitle = windowTitle;
        this.mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
        this.timer = new Timer();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (final Exception e) {
            e.printStackTrace(LoggerCompat.asPrintStream(LOGGER, Level.ERROR));
        } finally {
            cleanup();
        }
    }

    protected void init()  {
        this.window.init();
        this.timer.init();
        this.mouseInput.init(this.window);
        this.gameLogic.init(this.window);
        this.lastFps = this.timer.getTime();
        this.fps = 0;
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        final float interval = 1f / ConfigHandler.CONFIG.video.ups;
        this.running = true;
        while (this.running && !this.window.windowShouldClose()) {
            elapsedTime = this.timer.getElapsedTime();
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
        this.running = false;
    }

    protected void cleanup() {
        this.gameLogic.cleanup();
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
        this.gameLogic.update(interval, this.mouseInput, this.window);
    }

    protected void render() {
        if (this.window.getWindowOptions().showFps() && this.timer.getLastLoopTime() - this.lastFps > 1) {
            this.lastFps = this.timer.getLastLoopTime();
            this.window.setWindowTitle(this.windowTitle + " - " + this.fps + " FPS");
            this.fps = 0;
        }
        this.fps++;
        this.gameLogic.render(this.window);
        this.window.update();
    }

    protected boolean isRunning() {
        return this.running;
    }
}
