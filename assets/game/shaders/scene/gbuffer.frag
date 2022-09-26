#version 330

const int NUM_CASCADES = 3;

in vec2  vsTextcoord;
in vec3  vsNormal;
in vec4  vsMvVertexPos;
in vec4  vsMlightviewVertexPos[NUM_CASCADES];
in mat4  vsModelMatrix;
in float vsSelected;

layout (location = 0) out vec3 fsWorldpos;
layout (location = 1) out vec3 fsDiffuse;
layout (location = 2) out vec3 fsSpecular;
layout (location = 3) out vec3 fsNormal;
layout (location = 4) out vec2 fsShadow;

uniform mat4 viewMatrix;

struct Material {
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    int hasNormalMap;
    float reflectance;
};

uniform sampler2D textureSampler;
uniform sampler2D normalMap;
uniform Material material;

uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int renderShadow;

vec4 diffuseColour;
vec4 specularColour;

void getColour(Material material, vec2 textCoord) {
    if (material.hasTexture == 1) {
        diffuseColour = texture(textureSampler, textCoord);
        specularColour = diffuseColour;
        return;
    }
    diffuseColour = material.diffuse;
    specularColour = material.specular;
}

vec3 calcNormal(Material material, vec3 normal, vec2 textCoord, mat4 modelMatrix) {
    vec3 newNormal = normal;
    if (material.hasNormalMap == 1) {
        newNormal = texture(normalMap, textCoord).rgb;
        newNormal = normalize(newNormal * 2 - 1);
        newNormal = normalize(viewMatrix * modelMatrix * vec4(newNormal, 0.0)).xyz;
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
    for (int row = -1; row <= 1; ++row) {
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
    getColour(material, vsTextcoord);

    fsWorldpos   = vsMvVertexPos.xyz;
    fsDiffuse    = diffuseColour.xyz;
    fsSpecular   = specularColour.xyz;
    fsNormal     = normalize(calcNormal(material, vsNormal, vsTextcoord, vsModelMatrix));

    int idx;
    for (int i = 0; i < NUM_CASCADES; i++) {
        if (abs(vsMvVertexPos.z) < cascadeFarPlanes[i]) {
            idx = i;
            break;
        }
    }
	fsShadow  = vec2(calcShadow(vsMlightviewVertexPos[idx], idx), material.reflectance);

    if (vsSelected > 0) {
        fsDiffuse = vec3(fsDiffuse.x, fsDiffuse.y, 1);
    }
}