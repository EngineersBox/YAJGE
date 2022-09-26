#version 330

uniform mat4 projectionMatrix;
uniform mat4 viewModelMatrix;

in vec4 a_vertex;
in vec2 texCoord;

out vec2 outTexCoord;

void main(void) {
	outTexCoord = texCoord;

	gl_Position = projectionMatrix * viewModelMatrix * a_vertex;
}