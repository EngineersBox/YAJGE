#version 330

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in vec4 jointWeights;
layout (location=4) in ivec4 jointIndices;
layout (location=5) in mat4 viewModelInstancedMatrix;
layout (location=9) in mat4 modelLightViewInstancedMatrix;
layout (location=13) in vec2 texOffset;

out vec2 outTexCoord;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out vec4 mlightviewVertexPos;
out mat4 outViewModelMatrix;

uniform int isInstanced;
uniform mat4 viewModelNonInstancedMatrix;
uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 projectionMatrix;
uniform mat4 modelLightViewNonInstancedMatrix;
uniform mat4 orthoProjectionMatrix;

uniform int cols;
uniform int rows;

void main() {
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    mat4 viewModelMatrix;
    mat4 lightViewMatrix;
    if (isInstanced > 0) {
        viewModelMatrix = viewModelInstancedMatrix;
        lightViewMatrix = modelLightViewInstancedMatrix;

        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    } else {
        viewModelMatrix = viewModelNonInstancedMatrix;
        lightViewMatrix = modelLightViewNonInstancedMatrix;

        int count = 0;
        for (int i = 0; i < MAX_WEIGHTS; i++) {
            float weight = jointWeights[i];
            if (weight > 0) {
                count++;
                int jointIndex = jointIndices[i];
                vec4 tmpPos = jointsMatrix[jointIndex] * vec4(position, 1.0);
                initPos += weight * tmpPos;

                vec4 tmpNormal = jointsMatrix[jointIndex] * vec4(vertexNormal, 0.0);
                initNormal += weight * tmpNormal;
            }
        }
        if (count == 0) {
            initPos = vec4(position, 1.0);
            initNormal = vec4(vertexNormal, 0.0);
        }
    }
    vec4 mvPos = viewModelMatrix * initPos;
    gl_Position = projectionMatrix * mvPos;

    float x = (texCoord.x / cols + texOffset.x);
    float y = (texCoord.y / rows + texOffset.y);
    outTexCoord = vec2(x, y);

    mvVertexNormal = normalize(viewModelMatrix * initNormal).xyz;
    mvVertexPos = mvPos.xyz;
    mlightviewVertexPos = orthoProjectionMatrix * lightViewMatrix * initPos;
    outViewModelMatrix = viewModelMatrix;
}