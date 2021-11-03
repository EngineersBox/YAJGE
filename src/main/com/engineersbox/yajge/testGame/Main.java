package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.Engine;
import com.engineersbox.yajge.core.engine.IEngineLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger("TestGame");

    public static void main(final String[] args) {
        try {
            final IEngineLogic gameInstance = new TestGame();
            final Engine engine = new Engine(
                    "YAJGE",
                    gameInstance
            );
            engine.run();
        } catch (final Exception e) {
            LOGGER.error(e);
        }
    }
}