package com.engineersbox.yajge.core.window;

import com.engineersbox.yajge.resources.config.io.ConfigHandler;

public record WindowOptions(boolean cullFace,
                            boolean showTriangles,
                            boolean showFps,
                            boolean compatibleProfile,
                            boolean antialiasing,
                            boolean frustumCulling,
                            int width,
                            int height) {

    public static WindowOptions createFromConfig() {
        return new WindowOptions(
                ConfigHandler.CONFIG.engine.glOptions.cullface,
                ConfigHandler.CONFIG.engine.glOptions.showTrianges,
                ConfigHandler.CONFIG.engine.features.showFPS,
                ConfigHandler.CONFIG.engine.glOptions.compatProfile,
                ConfigHandler.CONFIG.engine.glOptions.antialiasing,
                ConfigHandler.CONFIG.render.camera.frustrumCulling,
                ConfigHandler.CONFIG.video.width,
                ConfigHandler.CONFIG.video.height
        );
    }
}
