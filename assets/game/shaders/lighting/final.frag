#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;
const int NUM_CASCADES = 3;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec4 mlightviewVertexPos[NUM_CASCADES];
in mat4 outViewModelMatrix;
in float outSelected;

out vec4 fragColor;

struct Attenuation {
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

struct SpotLight {
    PointLight pl;
    vec3 coneDir;
    float cutoff;
};

struct DirectionalLight {
    vec3 colour;
    vec3 direction;
    float intensity;
};

struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    int hasNormalMap;
    float reflectance;
};

struct Fog {
    int isActive;
    vec3 colour;
    float density;
};

uniform sampler2D textureSampler;
uniform sampler2D normalMap;
uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
uniform Fog fog;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform int renderShadow;

vec4 ambientColour;
vec4 diffuseColour;
vec4 specularColour;

void configureColours(Material material, vec2 textCoord) {
    if (material.hasTexture == 1) {
        ambientColour = texture(textureSampler, textCoord);
        diffuseColour = ambientColour;
        specularColour = ambientColour;
        return;
    }
    ambientColour = material.ambient;
    diffuseColour = material.diffuse;
    specularColour = material.specular;
}

vec4 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 toLightDir, vec3 normal) {
    vec4 accDiffuseColour = vec4(0, 0, 0, 0);
    vec4 accSpecularColour = vec4(0, 0, 0, 0);

    float diffuseFactor = max(dot(normal, toLightDir), 0.0);
    accDiffuseColour = diffuseColour * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 fromLightDir = -toLightDir;
    vec3 reflectedLight = normalize(reflect(fromLightDir , normal));
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    accSpecularColour = specularColour * light_intensity  * specularFactor * material.reflectance * vec4(light_colour, 1.0);

    return (accDiffuseColour + accSpecularColour);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.position - position;
    vec3 toLightDir = normalize(lightDirection);
    vec4 lightColour = calcLightColour(light.colour, light.intensity, position, toLightDir, normal);

    float distance = length(lightDirection);
    float attenuationInv = light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance;
    return lightColour / attenuationInv;
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.pl.position - position;
    vec3 toLightDir = normalize(lightDirection);
    vec3 fromLightDir = -toLightDir;
    float spotAlpha = dot(fromLightDir, normalize(light.coneDir));

    vec4 colour = vec4(0, 0, 0, 0);

    if (spotAlpha > light.cutoff)  {
        colour = calcPointLight(light.pl, position, normal);
        colour *= 1.0 - (1.0 - spotAlpha) / (1.0 - light.cutoff);
    }
    return colour;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal) {
    return calcLightColour(light.colour, light.intensity, position, normalize(light.direction), normal);
}

vec4 calcFog(vec3 pos, vec4 colour, Fog fog, vec3 ambientLight, DirectionalLight dirLight) {
    vec3 fogColor = fog.colour * (ambientLight + dirLight.colour * dirLight.intensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp((distance * fog.density)* (distance * fog.density));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 resultColour = mix(fogColor, colour.xyz, fogFactor);
    return vec4(resultColour.xyz, colour.w);
}

vec3 calcNormal(Material material, vec3 normal, vec2 text_coord, mat4 modelViewMatrix) {
    vec3 newNormal = normal;
    if (material.hasNormalMap == 1) {
        newNormal = texture(normalMap, text_coord).rgb;
        newNormal = normalize(newNormal * 2 - 1);
        newNormal = normalize(modelViewMatrix * vec4(newNormal, 0.0)).xyz;
    }
    return newNormal;
}

float calcShadow(vec4 position, int idx) {
    if (renderShadow == 0) {
        return 1.0;
    }

    vec3 projCoords = position.xyz;
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.005;

    float shadowFactor = 0.0;
    vec2 inc;
    if (idx == 0) {
        inc = 1.0 / textureSize(shadowMap_0, 0);
    } else if (idx == 1) {
        inc = 1.0 / textureSize(shadowMap_1, 0);
    } else {
        inc = 1.0 / textureSize(shadowMap_2, 0);
    }
    for(int row = -1; row <= 1; ++row) {
        for(int col = -1; col <= 1; ++col) {
            float textDepth;
            if (idx == 0) {
                textDepth = texture(shadowMap_0, projCoords.xy + vec2(row, col) * inc).r;
            } else if (idx == 1) {
                textDepth = texture(shadowMap_1, projCoords.xy + vec2(row, col) * inc).r;
            } else {
                textDepth = texture(shadowMap_2, projCoords.xy + vec2(row, col) * inc).r;
            }
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }
    shadowFactor /= 9.0;

    if(projCoords.z > 1.0) {
        shadowFactor = 1.0;
    }

    return 1 - shadowFactor;
}

void main() {
    configureColours(material, outTexCoord);
    vec3 currNomal = calcNormal(material, mvVertexNormal, outTexCoord, outViewModelMatrix);
    vec4 diffuseSpecularComposition = calcDirectionalLight(directionalLight, mvVertexPos, currNomal);

    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0) {
            diffuseSpecularComposition += calcPointLight(pointLights[i], mvVertexPos, currNomal);
        }
    }

    for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
        if (spotLights[i].pl.intensity > 0) {
            diffuseSpecularComposition += calcSpotLight(spotLights[i], mvVertexPos, currNomal);
        }
    }
    int idx;
    for (int i = 0; i < NUM_CASCADES; i++) {
        if (abs(mvVertexPos.z) < cascadeFarPlanes[i]) {
            idx = i;
            break;
        }
    }
    float shadow = calcShadow(mlightviewVertexPos[idx], idx);
    fragColor = clamp(ambientColour * vec4(ambientLight, 1) + diffuseSpecularComposition * shadow, 0, 1);
    if (fog.isActive == 1) {
        fragColor = calcFog(mvVertexPos, fragColor, fog, ambientLight, directionalLight);
    }

    if (outSelected > 0) {
        fragColor = vec4(fragColor.x, fragColor.y, 1, 1);
    }
}