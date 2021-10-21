package com.engineersbox.yajge.scene.gui;

import com.engineersbox.yajge.scene.object.SceneElement;

public interface IHud {
    SceneElement[] getSceneElements();

    default void cleanup() {
        for (final SceneElement sceneElement : getSceneElements()) {
            sceneElement.getMesh().cleanUp();
        }
    }
}
