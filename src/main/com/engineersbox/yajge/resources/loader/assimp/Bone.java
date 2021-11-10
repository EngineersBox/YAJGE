package com.engineersbox.yajge.resources.loader.assimp;

import org.joml.Matrix4f;

public record Bone(int boneId,
                   String boneName,
                   Matrix4f offsetMatrix) {}
