#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;

uniform mat4 viewModelMatrix;
uniform mat4 projectionMatrix;

uniform float texXOffset;
uniform float texYOffset;
uniform int cols;
uniform int rows;

void main() {
    gl_Position = projectionMatrix * viewModelMatrix * vec4(position, 1.0);
    
    float x = texCoord.x / cols + texXOffset;
    float y = texCoord.y / rows + texYOffset;

    outTexCoord = vec2(x, y);
}