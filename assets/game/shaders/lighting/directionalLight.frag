#version 330

out vec4 fragColor;

struct DirectionalLight {
    vec3 colour;
    vec3 direction;
    float intensity;
};

uniform sampler2D positionsText;
uniform sampler2D diffuseText;
uniform sampler2D specularText;
uniform sampler2D normalsText;
uniform sampler2D shadowText;
uniform sampler2D depthText;

uniform vec2 screenSize;

uniform float specularPower;
uniform vec3 ambientLight;
uniform DirectionalLight directionalLight;

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

vec4 calcDirectionalLight(vec4 diffuseC, vec4 speculrC, float reflectance, DirectionalLight light, vec3 position, vec3 normal) {
    return calcLightColour(diffuseC, speculrC, reflectance, light.colour, light.intensity, position, normalize(light.direction), normal);
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

    vec4 diffuseSpecularComp = calcDirectionalLight(diffuseC, speculrC, reflectance, directionalLight, worldPos.xyz, normal.xyz);

    fragColor = clamp(diffuseC * vec4(ambientLight, 1) + diffuseSpecularComp * shadowFactor, 0, 1);
}
