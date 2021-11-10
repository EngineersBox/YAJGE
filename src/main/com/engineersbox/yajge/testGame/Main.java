package com.engineersbox.yajge.testgame;

import com.engineersbox.yajge.core.engine.Engine;
import com.engineersbox.yajge.core.engine.IGameLogic;

public class Main {

    public static void main(final String[] args) {
        try {
            final IGameLogic gameLogic = new TestGame();
            final Engine gameEng = new Engine("GAME", gameLogic);
            gameEng.run();
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
