package com.engineersbox.yajge.testGame;

import com.engineersbox.yajge.engine.Engine;
import com.engineersbox.yajge.engine.core.EngineLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger("TestGame");

    public static void main(final String[] args) {
        try {
            final EngineLogic gameLogic = new TestGame();
            final Engine engine = new Engine(
                    "YAJGE",
                    600,
                    480,
                    true,
                    gameLogic
            );
            engine.run();
        } catch (final Exception e) {
            LOGGER.error(e);
        }
    }

}
