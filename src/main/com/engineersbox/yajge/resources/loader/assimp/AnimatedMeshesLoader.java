package com.engineersbox.yajge.resources.loader.assimp;

import com.engineersbox.yajge.animation.AnimatedFrame;
import com.engineersbox.yajge.animation.Animation;
import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.util.ListUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;

public class AnimatedMeshesLoader extends StaticMeshesLoader {

    public static AnimatedSceneElement loadAnimatedSceneElement(final String resourcePath,
                                                                final String texturesDir)  {
        return loadAnimatedSceneElement(
                resourcePath,
                texturesDir,
                aiProcess_GenSmoothNormals
                        | aiProcess_JoinIdenticalVertices
                        | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals
                        | aiProcess_LimitBoneWeights);
    }

    public static AnimatedSceneElement loadAnimatedSceneElement(final String resourcePath,
                                                                final String texturesDir,
                                                                final int flags) {
        final AIScene aiScene = aiImportFile(resourcePath, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model");
        }

        final int numMaterials = aiScene.mNumMaterials();
        final PointerBuffer aiMaterials = aiScene.mMaterials();
        if (aiMaterials == null) {
            throw new RuntimeException("Could not retrieve materials");
        }
        final List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        final List<Bone> boneList = new ArrayList<>();
        final int numMeshes = aiScene.mNumMeshes();
        final PointerBuffer aiMeshes = aiScene.mMeshes();
        if (aiMeshes == null) {
            throw new RuntimeException("Could not retrieve meshes");
        }
        final Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            final AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            final Mesh mesh = processMesh(aiMesh, materials, boneList);
            meshes[i] = mesh;
        }
        final AINode sceneRoot = aiScene.mRootNode();
        if (sceneRoot == null) {
            throw new RuntimeException("Could not retrieve scene root node");
        }
        final Node rootNode = buildNodesTree(sceneRoot, null);
        final Matrix4f globalInverseTransformation = toMatrix(sceneRoot.mTransformation()).invert();
        final Map<String, Animation> animations = processAnimations(
                aiScene,
                boneList,
                rootNode,
                globalInverseTransformation
        );
        return new AnimatedSceneElement(meshes, animations);
    }

    private static Node buildNodesTree(final AINode aiNode,
                                       final Node parentNode) {
        final String nodeName = aiNode.mName().dataString();
        final Node node = new Node(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        final int numChildren = aiNode.mNumChildren();
        final PointerBuffer aiChildren = aiNode.mChildren();
        if (aiChildren == null) {
            throw new RuntimeException("Could not retrieve node children");
        }
        for (int i = 0; i < numChildren; i++) {
            final AINode aiChildNode = AINode.create(aiChildren.get(i));
            final Node childNode = buildNodesTree(aiChildNode, node);
            node.addChild(childNode);
        }
        return node;
    }

    private static Map<String, Animation> processAnimations(final AIScene aiScene,
                                                            final List<Bone> boneList,
                                                            final Node rootNode,
                                                            final Matrix4f globalInverseTransformation) {
        final Map<String, Animation> animations = new HashMap<>();
        final int numAnimations = aiScene.mNumAnimations();
        final PointerBuffer aiAnimations = aiScene.mAnimations();
        if (aiAnimations == null) {
            return animations;
        }
        for (int i = 0; i < numAnimations; i++) {
            final AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));
            final int maxFrames = calcAnimationMaxFrames(aiAnimation);

            final List<AnimatedFrame> frames = new ArrayList<>();
            final Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());
            animations.put(animation.getName(), animation);

            for (int j = 0; j < maxFrames; j++) {
                final AnimatedFrame animatedFrame = new AnimatedFrame();
                buildFrameMatrices(aiAnimation, boneList, animatedFrame, j, rootNode,
                        rootNode.getNodeTransform(), globalInverseTransformation);
                frames.add(animatedFrame);
            }
        }
        return animations;
    }

    private static void buildFrameMatrices(final AIAnimation aiAnimation,
                                           final List<Bone> boneList,
                                           final AnimatedFrame animatedFrame,
                                           final int frame,
                                           final Node node,
                                           final Matrix4f parentTransformation,
                                           final Matrix4f globalInverseTransform) {
        final String nodeName = node.getName();
        final AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
        Matrix4f nodeTransform = node.getNodeTransform();
        if (aiNodeAnim != null) {
            nodeTransform = buildNodeTransformationMatrix(aiNodeAnim, frame);
        }
        final Matrix4f nodeGlobalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);

        final List<Bone> affectedBones = boneList.stream()
                .filter(b -> b.boneName().equals(nodeName))
                .toList();
        for (final Bone bone: affectedBones) {
            final Matrix4f boneTransform = new Matrix4f(globalInverseTransform)
                    .mul(nodeGlobalTransform)
                    .mul(bone.offsetMatrix());
            animatedFrame.setMatrix(bone.boneId(), boneTransform);
        }

        for (final Node childNode : node.getChildren()) {
            buildFrameMatrices(
                    aiAnimation,
                    boneList,
                    animatedFrame,
                    frame,
                    childNode,
                    nodeGlobalTransform,
                    globalInverseTransform
            );
        }
    }

    private static Matrix4f buildNodeTransformationMatrix(final AINodeAnim aiNodeAnim,
                                                          final int frame) {
        final AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        if (positionKeys == null) {
            throw new RuntimeException("Could not retrieve position keys");
        }
        final AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        if (scalingKeys == null) {
            throw new RuntimeException("Could not retrieve scaling keys");
        }
        final AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();
        if (rotationKeys == null) {
            throw new RuntimeException("Could not retrieve rotation keys");
        }

        AIVectorKey aiVecKey;
        AIVector3D vec;

        final Matrix4f nodeTransform = new Matrix4f();
        final int numPositions = aiNodeAnim.mNumPositionKeys();
        if (numPositions > 0) {
            aiVecKey = positionKeys.get(Math.min(numPositions - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.translate(vec.x(), vec.y(), vec.z());
        }
        final int numRotations = aiNodeAnim.mNumRotationKeys();
        if (numRotations > 0) {
            final AIQuatKey quatKey = rotationKeys.get(Math.min(numRotations - 1, frame));
            final AIQuaternion aiQuaternion = quatKey.mValue();
            final Quaternionf quaternion = new Quaternionf(
                    aiQuaternion.x(),
                    aiQuaternion.y(),
                    aiQuaternion.z(),
                    aiQuaternion.w()
            );
            nodeTransform.rotate(quaternion);
        }
        final int numScalingKeys = aiNodeAnim.mNumScalingKeys();
        if (numScalingKeys > 0) {
            aiVecKey = scalingKeys.get(Math.min(numScalingKeys - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.scale(vec.x(), vec.y(), vec.z());
        }

        return nodeTransform;
    }

    private static AINodeAnim findAIAnimNode(final AIAnimation aiAnimation,
                                             final String nodeName) {
        AINodeAnim result = null;
        final int numAnimNodes = aiAnimation.mNumChannels();
        final PointerBuffer aiChannels = aiAnimation.mChannels();
        if (aiChannels == null) {
            throw new RuntimeException("Could not retrieve channels");
        }
        for (int i = 0; i < numAnimNodes; i++) {
            final AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            if ( nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }
        return result;
    }

    private static int calcAnimationMaxFrames(final AIAnimation aiAnimation) {
        int maxFrames = 0;
        final int numNodeAnims = aiAnimation.mNumChannels();
        final PointerBuffer aiChannels = aiAnimation.mChannels();
        if (aiChannels == null) {
            throw new RuntimeException("Could not retrieve channels");
        }
        for (int i=0; i<numNodeAnims; i++) {
            final AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            final int numFrames = Math.max(Math.max(aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()),
                    aiNodeAnim.mNumRotationKeys());
            maxFrames = Math.max(maxFrames, numFrames);
        }

        return maxFrames;
    }

    private static void processBones(final AIMesh aiMesh,
                                     final List<Bone> boneList,
                                     final List<Integer> boneIds,
                                     final List<Float> weights) {
        final Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        final int numBones = aiMesh.mNumBones();
        final PointerBuffer aiBones = aiMesh.mBones();
        if (aiBones == null) {
            throw new RuntimeException("Could not retrieve bones");
        }
        for (int i = 0; i < numBones; i++) {
            final AIBone aiBone = AIBone.create(aiBones.get(i));
            final int id = boneList.size();
            final Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
            boneList.add(bone);
            final int numWeights = aiBone.mNumWeights();
            final AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                final AIVertexWeight aiWeight = aiWeights.get(j);
                final VertexWeight vw = new VertexWeight(bone.boneId(), aiWeight.mVertexId(),
                        aiWeight.mWeight());
                final List<VertexWeight> vertexWeightList = weightSet.computeIfAbsent(vw.getVertexId(), k -> new ArrayList<>());
                vertexWeightList.add(vw);
            }
        }

        final int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            final List<VertexWeight> vertexWeightList = weightSet.get(i);
            if (vertexWeightList == null || vertexWeightList.isEmpty()) {
                return;
            }
            for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
                if (j < vertexWeightList.size()) {
                    final VertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.getWeight());
                    boneIds.add(vw.getBoneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
    }

    private static Mesh processMesh(final AIMesh aiMesh,
                                    final List<Material> materials,
                                    final List<Bone> boneList) {
        final List<Integer> boneIds = new ArrayList<>();
        final List<Float> weights = new ArrayList<>();

        final List<Float> vertices = processVertices(aiMesh);
        final List<Float> normals = processNormals(aiMesh);
        final List<Float> textures = processTextCoords(aiMesh);
        final List<Integer> indices = processIndices(aiMesh);
        processBones(
                aiMesh,
                boneList,
                boneIds,
                weights
        );

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
                ListUtils.intListToArray(indices),
                ListUtils.intListToArray(boneIds),
                ListUtils.floatListToArray(weights)
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

    private static Matrix4f toMatrix(final AIMatrix4x4 aiMatrix4x4) {
        final Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }
}
