#version 330

out vec4 fragColor;

struct Attenuation{
    float constant;
    float linear;
    float exponent;
};

struct PointLight {
    vec3 colour;
    vec3 position;
    float intensity;
    Attenuation att;
};

uniform sampler2D positionsText;
uniform sampler2D diffuseText;
uniform sampler2D specularText;
uniform sampler2D normalsText;
uniform sampler2D shadowText;
uniform sampler2D depthText;

uniform vec2 screenSize;

uniform float specularPower;
uniform PointLight pointLight;

vec2 getTextCoord() {
    return gl_FragCoord.xy / screenSize;
}

vec4 calcLightColour(vec4 diffuseC, vec4 speculrC, float reflectance, vec3 lightColour, float lightIntensity, vec3 position, vec3 toLightDir, vec3 normal) {
    vec4 diffuseColour = vec4(0, 0, 0, 1);
    vec4 specColour = vec4(0, 0, 0, 1);

    float diffuseFactor = max(dot(normal, toLightDir), 0.0);
    diffuseColour = diffuseC * vec4(lightColour, 1.0) * lightIntensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 fromLightDir = -toLightDir;
    vec3 reflectedLight = normalize(reflect(fromLightDir , normal));
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * lightIntensity  * specularFactor * reflectance * vec4(lightColour, 1.0);

    return (diffuseColour + specColour);
}

vec4 calcPointLight(vec4 diffuseC, vec4 speculrC, float reflectance, PointLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.position - position;
    vec3 toLightDir  = normalize(lightDirection);
    vec4 lightColour = calcLightColour(diffuseC, speculrC, reflectance, light.colour, light.intensity, position, toLightDir, normal);

    float distance = length(lightDirection);
    float attenuationInv = light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance;
    return lightColour / attenuationInv;
}

void main() {
    vec2 textCoord = getTextCoord();
    float depth = texture(depthText, textCoord).r;
    vec3 worldPos = texture(positionsText, textCoord).xyz;
    vec4 diffuseC = texture(diffuseText, textCoord);
    vec4 speculrC = texture(specularText, textCoord);
    vec3 normal  = texture(normalsText, textCoord).xyz;
	float shadowFactor = texture(shadowText, textCoord).r;
	float reflectance = texture(shadowText, textCoord).g;

	fragColor = calcPointLight(diffuseC, speculrC, reflectance, pointLight, worldPos.xyz, normal.xyz) * shadowFactor;
}