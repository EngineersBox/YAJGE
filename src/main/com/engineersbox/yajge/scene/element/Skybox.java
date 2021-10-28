package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.rendering.assets.materials.Material;
import com.engineersbox.yajge.rendering.assets.materials.Texture;
import com.engineersbox.yajge.rendering.object.composite.Mesh;
import com.engineersbox.yajge.resources.loader.OBJLoader;

public class Skybox extends SceneElement {

    public Skybox(final String objModel,
                  final String textureFile) {
        super();
        final Mesh mesh = OBJLoader.loadMesh(objModel);
        mesh.setMaterial(new Material(new Texture(textureFile), 0.0f));
        setMesh(mesh);
        setPosition(0, 0, 0);
    }
}
