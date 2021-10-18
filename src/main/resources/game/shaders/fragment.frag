#version 330

in  vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 colour;
uniform int useColour;

void main(){
    if (useColour == 1) {
        fragColor = vec4(colour, 1);
    } else {
        fragColor = texture(textureSampler, outTexCoord);
    }
}