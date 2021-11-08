package com.engineersbox.yajge.resources.loader.assimp;

import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.util.ListUtils;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class StaticMeshLoader {

    StaticMeshLoader() {
        throw new IllegalStateException("Static accessor class");
    }

    public static Mesh[] load(final String resourcePath,
                              final String texturesDir) {
        return load(
                resourcePath,
                texturesDir,
                aiProcess_GenSmoothNormals
                        | aiProcess_JoinIdenticalVertices
                        | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals
                        | aiProcess_PreTransformVertices
        );
    }

    public static Mesh[] load(final String resourcePath,
                              final String texturesDir,
                              final int flags) {
        final AIScene aiScene = aiImportFile(resourcePath, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model");
        }

        final int numMaterials = aiScene.mNumMaterials();
        final PointerBuffer aiMaterials = aiScene.mMaterials();
        if (aiMaterials == null) {
            throw new RuntimeException("Could not get materials");
        }
        final List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(
                    aiMaterial,
                    materials,
                    texturesDir
            );
        }

        final int numMeshes = aiScene.mNumMeshes();
        final PointerBuffer aiMeshes = aiScene.mMeshes();
        if (aiMeshes == null) {
            throw new RuntimeException("Could not get meshes");
        }
        final Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            meshes[i] = processMesh(
                    AIMesh.create(aiMeshes.get(i)),
                    materials
            );
        }

        return meshes;
    }

    protected static List<Integer> processIndices(final AIMesh aiMesh) {
        final List<Integer> indices = new ArrayList<>();
        final int numFaces = aiMesh.mNumFaces();
        final AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            final AIFace aiFace = aiFaces.get(i);
            final IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        return indices;
    }

    protected static void processMaterial(final AIMaterial aiMaterial,
                                          final List<Material> materials,
                                          final String texturesDir) {
        final AIColor4D colour = AIColor4D.create();
        final AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(
                aiMaterial,
                aiTextureType_DIFFUSE,
                0,
                path,
                (IntBuffer) null,
                null,
                null,
                null,
                null,
                null
        );
        final String textPath = path.dataString();
        Texture texture = null;
        if (!textPath.isEmpty()) {
            final TextureCache textCache = TextureCache.getInstance();
            String textureFile = "";
            if (texturesDir != null && !texturesDir.isEmpty()) {
                textureFile += texturesDir + "/";
            }
            textureFile += textPath;
            textureFile = textureFile.replace("//", "/");
            texture = textCache.getTexture(textureFile);
        }

        final Material material = new Material(
                getLayerColor(AI_MATKEY_COLOR_AMBIENT, aiMaterial, colour),
                getLayerColor(AI_MATKEY_COLOR_DIFFUSE, aiMaterial, colour),
                getLayerColor(AI_MATKEY_COLOR_SPECULAR, aiMaterial, colour),
                1.0f
        );
        material.setTexture(texture);
        materials.add(material);
    }

    private static Vector4f getLayerColor(final String layerName,
                                          final AIMaterial material,
                                          final AIColor4D colour) {
        final int result = aiGetMaterialColor(
                material,
                layerName,
                aiTextureType_NONE,
                0,
                colour
        );
        return result == 0 ? new Vector4f(colour.r(),colour.g(), colour.b(), colour.a()) : Material.DEFAULT_COLOUR;
    }

    private static Mesh processMesh(final AIMesh aiMesh,
                                    final List<Material> materials) {
        final List<Float> vertices = processVertices(aiMesh);
        final List<Float> normals = processNormals(aiMesh);
        final List<Float> textures = processTexCoords(aiMesh);
        final List<Integer> indices = processIndices(aiMesh);

        if (textures.isEmpty()) {
            final int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                textures.add(0.0f);
            }
        }

        final Mesh mesh = new Mesh(
                ListUtils.floatListToArray(vertices),
                ListUtils.floatListToArray(textures),
                ListUtils.floatListToArray(normals),
                ListUtils.intListToArray(indices)
        );
        final Material material;
        final int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material();
        }
        mesh.setMaterial(material);
        return mesh;
    }

    protected static List<Float> processNormals(final AIMesh aiMesh) {
        final List<Float> normals = new ArrayList<>();
        final AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        if (aiNormals == null) {
            return normals;
        }
        while (aiNormals.remaining() > 0) {
            final AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }
        return normals;
    }

    protected static List<Float> processTexCoords(final AIMesh aiMesh) {
        final List<Float> textures = new ArrayList<>();
        final AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
        if (textCoords == null) {
            return textures;
        }
        final int numTextCoords = textCoords.remaining();
        for (int i = 0; i < numTextCoords; i++) {
            final AIVector3D textCoord = textCoords.get();
            textures.add(textCoord.x());
            textures.add(1 - textCoord.y());
        }
        return textures;
    }

    protected static List<Float> processVertices(final AIMesh aiMesh) {
        final List<Float> vertices = new ArrayList<>();
        final AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            final AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
        return vertices;
    }
}
