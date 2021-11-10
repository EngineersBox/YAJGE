package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.loader.assimp.StaticMeshesLoader;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.Vector4f;

public class Skybox extends SceneElement {

    public Skybox(final String objModel,
                  final String textureFile)  {
        super();
        final Mesh skyBoxMesh = StaticMeshesLoader.load(objModel, "")[0];
        skyBoxMesh.setMaterial(new Material(new Texture(textureFile), 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }

    public Skybox(final String objModel,
                  final Vector4f colour)  {
        super();
        final Mesh skyBoxMesh = StaticMeshesLoader.load(objModel, "", 0)[0];
        skyBoxMesh.setMaterial(new Material(colour, 0));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
