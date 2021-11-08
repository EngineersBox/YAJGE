package com.engineersbox.yajge.resources.loader.assimp;

import com.engineersbox.yajge.resources.assets.material.Texture;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

    private static TextureCache INSTANCE;
    private static final long TTL = 60;
    private static final long SYNC_INTERVAL = 5;
    private static final int MAX_ITEMS = 1000;
    private final Map<String, Texture> textures;

    private TextureCache() {
        this.textures = new HashMap<>();//new SyncCache<>(TTL, SYNC_INTERVAL, MAX_ITEMS);
    }

    public static synchronized TextureCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TextureCache();
        }
        return INSTANCE;
    }

    public Texture getTexture(final String path) {
        return this.textures.computeIfAbsent(path, Texture::new);
    }
}