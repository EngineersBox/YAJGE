#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=5) in mat4 viewModelMatrix;
layout (location=13) in vec2 texOffset;

out vec2 outTexCoord;

uniform mat4 projectionMatrix;
uniform int cols;
uniform int rows;

void main() {
    gl_Position = projectionMatrix * viewModelMatrix * vec4(position, 1.0);
    float x = (texCoord.x / cols + texOffset.x);
    float y = (texCoord.y / rows + texOffset.y);
    outTexCoord = vec2(x, y);
}