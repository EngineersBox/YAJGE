package com.engineersbox.yajge.scene.gui;

import com.engineersbox.yajge.scene.element.SceneElement;

public interface IHud {

    SceneElement[] getSceneElements();

    default void cleanup() {
        final SceneElement[] sceneElements = getSceneElements();
        for (final SceneElement sceneElement : sceneElements) {
            sceneElement.getMesh().cleanUp();
        }
    }
}
