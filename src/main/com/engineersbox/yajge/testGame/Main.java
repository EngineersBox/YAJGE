package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.Engine;
import com.engineersbox.yajge.core.engine.IGameLogic;
import com.engineersbox.yajge.core.window.WindowOptions;
import com.engineersbox.yajge.logging.LoggerCompat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger("TestGame");

    public static void main(final String[] args) {
        try {
            final IGameLogic gameInstance = new TestGame();
            final WindowOptions opts = new WindowOptions();
            final Engine engine = new Engine(
                    "YAJGE",
                    true,
                    opts,
                    gameInstance
            );
            engine.run();
        } catch (final Exception e) {
            e.printStackTrace(LoggerCompat.asPrintStream(LOGGER, Level.ERROR));
        }
    }
}