package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.Engine;
import com.engineersbox.yajge.core.engine.IGameLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static void main(final String[] args) {
        try {
            final IGameLogic gameLogic = new TestGame();
            final Engine engine = new Engine("GAME", gameLogic);
            engine.run();
        } catch (final Exception e) {
            LOGGER.error(e);
            System.exit(1);
        }
    }
}
