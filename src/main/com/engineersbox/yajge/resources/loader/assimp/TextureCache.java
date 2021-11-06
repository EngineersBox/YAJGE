package com.engineersbox.yajge.resources.loader.assimp;

import com.engineersbox.yajge.resources.assets.material.Texture;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

    private static TextureCache INSTANCE;
    private final Map<String, Texture> textures;

    private TextureCache() {
        this.textures = new HashMap<>();
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