package com.engineersbox.yajge.scene.element;

import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.resources.loader.OBJLoader;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import org.joml.Vector4f;

public class Skybox extends SceneElement {

    public Skybox(final String objModel, final String textureFile) {
        super();
        final Mesh mesh = OBJLoader.loadMesh(objModel);
        final Texture texture = new Texture(textureFile);
        mesh.setMaterial(new Material(texture, 0.0f));
        setMesh(mesh);
        setPosition(0, 0, 0);
    }

    public Skybox(final String objModel, final Vector4f colour) {
        super();
        final Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        final Material material = new Material(colour, 0);
        skyBoxMesh.setMaterial(material);
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
