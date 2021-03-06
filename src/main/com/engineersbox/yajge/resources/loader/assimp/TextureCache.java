package com.engineersbox.yajge.resources.loader.assimp;

import com.engineersbox.yajge.resources.assets.material.Texture;

public class TextureCache {

    private static TextureCache INSTANCE;
    private static final long TTL = 60;
    private static final long SYNC_INTERVAL = 5;
    private static final int MAX_ITEMS = 1000;
    private final SyncCache<String, Texture> cacheMap;

    private TextureCache() {
        this.cacheMap = new SyncCache<>(TTL, SYNC_INTERVAL, MAX_ITEMS);
    }

    public static synchronized TextureCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TextureCache();
        }
        return INSTANCE;
    }

    public Texture getTexture(final String path)  {
        return this.cacheMap.computeIfAbsent(path, Texture::new);
    }
}