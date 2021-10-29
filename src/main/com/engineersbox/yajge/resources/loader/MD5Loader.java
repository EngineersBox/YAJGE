package com.engineersbox.yajge.resources.loader;

import com.engineersbox.yajge.animation.AnimVertex;
import com.engineersbox.yajge.animation.AnimatedFrame;
import com.engineersbox.yajge.resources.assets.material.Material;
import com.engineersbox.yajge.resources.assets.material.Texture;
import com.engineersbox.yajge.scene.element.animation.AnimatedSceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;
import com.engineersbox.yajge.scene.element.object.md5.anim.MD5AnimModel;
import com.engineersbox.yajge.scene.element.object.md5.frame.MD5BaseFrame;
import com.engineersbox.yajge.scene.element.object.md5.frame.MD5BaseFrameData;
import com.engineersbox.yajge.scene.element.object.md5.frame.MD5Frame;
import com.engineersbox.yajge.scene.element.object.md5.hierarchy.MD5HierarchyData;
import com.engineersbox.yajge.scene.element.object.md5.joint.MD5JointData;
import com.engineersbox.yajge.scene.element.object.md5.model.MD5Model;
import com.engineersbox.yajge.scene.element.object.md5.primitive.MD5Mesh;
import com.engineersbox.yajge.scene.element.object.md5.primitive.MD5Triangle;
import com.engineersbox.yajge.scene.element.object.md5.primitive.MD5Vertex;
import com.engineersbox.yajge.scene.element.object.md5.primitive.MD5Weight;
import com.engineersbox.yajge.util.FileUtils;
import com.engineersbox.yajge.util.ListUtils;
import com.engineersbox.yajge.util.MD5Utils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MD5Loader {

    public static AnimatedSceneElement process(final MD5Model md5Model, final MD5AnimModel animModel, final Vector4f defaultColour) {
        final List<Matrix4f> invJointMatrices = calcInJointMatrices(md5Model);
        final List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);

        final List<Mesh> list = new ArrayList<>();
        for (final MD5Mesh md5Mesh : md5Model.getMeshes()) {
            final Mesh mesh = generateMesh(md5Model, md5Mesh);
            handleTexture(mesh, md5Mesh, defaultColour);
            list.add(mesh);
        }

        Mesh[] meshes = new Mesh[list.size()];
        meshes = list.toArray(meshes);

        return new AnimatedSceneElement(meshes, animatedFrames, invJointMatrices);
    }

    private static List<Matrix4f> calcInJointMatrices(final MD5Model md5Model) {
        final List<Matrix4f> result = new ArrayList<>();
        final List<MD5JointData> joints = md5Model.getJointInfo().getJoints();
        for (final MD5JointData joint : joints) {
            final Matrix4f mat = new Matrix4f()
                    .translate(joint.getPosition())
                    .rotate(joint.getOrientation())
                    .invert();
            result.add(mat);
        }
        return result;
    }

    private static Mesh generateMesh(final MD5Model md5Model, final MD5Mesh md5Mesh) {
        final List<AnimVertex> vertices = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

        final List<MD5Vertex> md5Vertices = md5Mesh.getVertices();
        final List<MD5Weight> weights = md5Mesh.getWeights();
        final List<MD5JointData> joints = md5Model.getJointInfo().getJoints();

        for (final MD5Vertex md5Vertex : md5Vertices) {
            final AnimVertex vertex = new AnimVertex();
            vertices.add(vertex);

            vertex.position = new Vector3f();
            vertex.textCoords = md5Vertex.getTextCoords();

            final int startWeight = md5Vertex.getStartWeight();
            final int numWeights = md5Vertex.getWeightCount();

            vertex.jointIndices = new int[numWeights];
            Arrays.fill(vertex.jointIndices, -1);
            vertex.weights = new float[numWeights];
            Arrays.fill(vertex.weights, -1);
            for (int i = startWeight; i < startWeight + numWeights; i++) {
                final MD5Weight weight = weights.get(i);
                final MD5JointData joint = joints.get(weight.getJointIndex());
                final Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                final Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertex.position.add(acumPos);
                vertex.jointIndices[i - startWeight] = weight.getJointIndex();
                vertex.weights[i - startWeight] = weight.getBias();
            }
        }

        for (final MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());

            // Normals
            final AnimVertex v0 = vertices.get(tri.getVertex0());
            final AnimVertex v1 = vertices.get(tri.getVertex1());
            final AnimVertex v2 = vertices.get(tri.getVertex2());
            final Vector3f pos0 = v0.position;
            final Vector3f pos1 = v1.position;
            final Vector3f pos2 = v2.position;

            final Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));

            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        // Once the contributions have been added, normalize the result
        for(final AnimVertex v : vertices) {
            v.normal.normalize();
        }

        return createMesh(vertices, indices);
    }

    private static List<AnimatedFrame> processAnimationFrames(final MD5Model md5Model, final MD5AnimModel animModel, final List<Matrix4f> invJointMatrices) {
        final List<AnimatedFrame> animatedFrames = new ArrayList<>();
        final List<MD5Frame> frames = animModel.getFrames();
        for (final MD5Frame frame : frames) {
            final AnimatedFrame data = processAnimationFrame(md5Model, animModel, frame, invJointMatrices);
            animatedFrames.add(data);
        }
        return animatedFrames;
    }

    private static AnimatedFrame processAnimationFrame(final MD5Model md5Model, final MD5AnimModel animModel, final MD5Frame frame, final List<Matrix4f> invJointMatrices) {
        final AnimatedFrame result = new AnimatedFrame();

        final MD5BaseFrame baseFrame = animModel.getBaseFrame();
        final List<MD5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyDataList();

        final List<MD5JointData> joints = md5Model.getJointInfo().getJoints();
        final int numJoints = joints.size();
        final float[] frameData = frame.getFrameData();
        for (int i = 0; i < numJoints; i++) {
            final MD5JointData joint = joints.get(i);
            final MD5BaseFrameData baseFrameData = baseFrame.getFrameDataList().get(i);
            final Vector3f position = baseFrameData.getPosition();
            Quaternionf orientation = baseFrameData.getOrientation();

            final int flags = hierarchyList.get(i).getFlags();
            int startIndex = hierarchyList.get(i).getStartIndex();

            if ((flags & 1) > 0) {
                position.x = frameData[startIndex++];
            }
            if ((flags & 2) > 0) {
                position.y = frameData[startIndex++];
            }
            if ((flags & 4) > 0) {
                position.z = frameData[startIndex++];
            }
            if ((flags & 8) > 0) {
                orientation.x = frameData[startIndex++];
            }
            if ((flags & 16) > 0) {
                orientation.y = frameData[startIndex++];
            }
            if ((flags & 32) > 0) {
                orientation.z = frameData[startIndex++];
            }
            // Update Quaternion's w component
            orientation = MD5Utils.calculateQuaternion(orientation.x, orientation.y, orientation.z);

            // Calculate translation and rotation matrices for this joint
            final Matrix4f translateMat = new Matrix4f().translate(position);
            final Matrix4f rotationMat = new Matrix4f().rotate(orientation);
            Matrix4f jointMat = translateMat.mul(rotationMat);

            // Joint position is relative to joint's parent index position. Use parent matrices
            // to transform it to model space
            if (joint.getParentIndex() > -1) {
                final Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
                jointMat = new Matrix4f(parentMatrix).mul(jointMat);
            }

            result.setMatrix(i, jointMat, invJointMatrices.get(i));
        }

        return result;
    }

    private static Mesh createMesh(final List<AnimVertex> vertices, final List<Integer> indices) {
        final List<Float> positions = new ArrayList<>();
        final List<Float> textCoords = new ArrayList<>();
        final List<Float> normals = new ArrayList<>();
        final List<Integer> jointIndices = new ArrayList<>();
        final List<Float> weights = new ArrayList<>();

        for (final AnimVertex vertex : vertices) {
            positions.add(vertex.position.x);
            positions.add(vertex.position.y);
            positions.add(vertex.position.z);

            textCoords.add(vertex.textCoords.x);
            textCoords.add(vertex.textCoords.y);

            normals.add(vertex.normal.x);
            normals.add(vertex.normal.y);
            normals.add(vertex.normal.z);

            final int numWeights = vertex.weights.length;
            for (int i = 0; i < Mesh.MAX_WEIGHTS; i++) {
                if (i < numWeights) {
                    jointIndices.add(vertex.jointIndices[i]);
                    weights.add(vertex.weights[i]);
                } else {
                    jointIndices.add(-1);
                    weights.add(-1.0f);
                }
            }
        }

        return new Mesh(
                ListUtils.floatListToArray(positions),
                ListUtils.floatListToArray(textCoords),
                ListUtils.floatListToArray(normals),
                ListUtils.intListToArray(indices),
                ListUtils.intListToArray(jointIndices),
                ListUtils.floatListToArray(weights)
        );
    }

    private static void handleTexture(final Mesh mesh, final MD5Mesh md5Mesh, final Vector4f defaultColour) {
        final String texturePath = md5Mesh.getTexture();
        if (texturePath != null && !texturePath.isEmpty()) {
            final Texture texture = new Texture(texturePath);
            final Material material = new Material(texture);

            final int pos = texturePath.lastIndexOf(".");
            if (pos > 0) {
                final String basePath = texturePath.substring(0, pos);
                final String extension = texturePath.substring(pos);
                final String normalMapFileName = basePath + "_local" + extension;
                if (FileUtils.fileExists(normalMapFileName)) {
                    final Texture normalMap = new Texture(normalMapFileName);
                    material.setNormalMap(normalMap);
                }
            }
            mesh.setMaterial(material);
        } else {
            mesh.setMaterial(new Material(defaultColour, 1));
        }
    }
}
