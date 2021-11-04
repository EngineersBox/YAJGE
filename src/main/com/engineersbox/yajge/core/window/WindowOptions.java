package com.engineersbox.yajge.core.window;

import com.engineersbox.yajge.resources.config.io.ConfigHandler;

public record WindowOptions(boolean cullFace,
                            boolean showTriangles,
                            boolean compatProfile,
                            boolean antialiasing){

    public static WindowOptions createFromConfig() {
        return new WindowOptions(
                ConfigHandler.CONFIG.engine.glOptions.cullface,
                ConfigHandler.CONFIG.engine.glOptions.showTrianges,
                ConfigHandler.CONFIG.engine.glOptions.compatProfile,
                ConfigHandler.CONFIG.engine.glOptions.antialiasing
        );
    }

}
