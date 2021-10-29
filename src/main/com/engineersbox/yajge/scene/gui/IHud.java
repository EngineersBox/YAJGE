package com.engineersbox.yajge.scene.gui;

import com.engineersbox.yajge.scene.element.SceneElement;

public interface IHud {

    SceneElement[] getGameItems();

    default void cleanup() {
        final SceneElement[] sceneElements = getGameItems();
        for (final SceneElement sceneElement : sceneElements) {
            sceneElement.getMesh().cleanUp();
        }
    }
}
