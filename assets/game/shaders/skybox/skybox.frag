#version 330

in vec2 outTexCoord;
in vec3 mvPos;
out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 ambientLight;

void main() {
    fragColor = vec4(ambientLight, 1) * texture(textureSampler, outTexCoord);
}