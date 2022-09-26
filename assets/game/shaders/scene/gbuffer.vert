#version 330

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;
const int NUM_CASCADES = 3;

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in vec4 jointWeights;
layout (location=4) in ivec4 jointIndices;
layout (location=5) in mat4 modelInstancedMatrix;
layout (location=9) in vec2 texOffset;
layout (location=10) in float selectedInstanced;

uniform int isInstanced;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelNonInstancedMatrix;
uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 lightViewMatrix[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int cols;
uniform int rows;
uniform float selectedNonInstanced;

out vec2 vsTextcoord;
out vec3 vsNormal;
out vec4 vsMvVertexPos;
out vec4 vsMlightviewVertexPos[NUM_CASCADES];
out mat4 vsModelMatrix;
out float vsSelected;

void main() {
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    mat4 modelMatrix;
    if (isInstanced > 0) {
        vsSelected = selectedInstanced;
        modelMatrix = modelInstancedMatrix;

        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    } else {
        vsSelected = selectedNonInstanced;
        modelMatrix = modelNonInstancedMatrix;

        int count = 0;
        for(int i = 0; i < MAX_WEIGHTS; i++) {
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
	mat4 viewModelMatrix = viewMatrix * modelMatrix;
	vsMvVertexPos = viewModelMatrix * initPos;
    gl_Position = projectionMatrix * vsMvVertexPos;

    float x = (texCoord.x / cols + texOffset.x);
    float y = (texCoord.y / rows + texOffset.y);

    vsTextcoord = vec2(x, y);
    vsNormal = normalize(viewModelMatrix * initNormal).xyz;

    for (int i = 0; i < NUM_CASCADES; i++) {
        vsMlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * initPos;
    }
	
	vsModelMatrix = modelMatrix;
}