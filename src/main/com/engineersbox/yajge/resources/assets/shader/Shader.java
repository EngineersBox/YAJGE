package com.engineersbox.yajge.resources.assets.shader;

import com.engineersbox.yajge.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajge.rendering.scene.lighting.Attenuation;
import com.engineersbox.yajge.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajge.rendering.scene.lighting.PointLight;
import com.engineersbox.yajge.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajge.resources.assets.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private static final Logger LOGGER = LogManager.getLogger(Shader.class);

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private int geometryShaderId;

    private final Map<String, Integer> uniforms;

    public Shader() {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new RuntimeException("Could not create Shader");
        }
        this.uniforms = new HashMap<>();
    }

    public void createUniform(final String uniformName) {
        final int uniformLocation = glGetUniformLocation(this.programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform:" + uniformName);
        }
        this.uniforms.put(uniformName, uniformLocation);
    }

    public void createPointLightListUniform(final String uniformName, final int size) {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createPointLightUniform(final String uniformName) {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    public void createSpotLightListUniform(final String uniformName, final int size) {
        for (int i = 0; i < size; i++) {
            createSpotLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createSpotLightUniform(final String uniformName) {
        createPointLightUniform(uniformName + ".pl");
        createUniform(uniformName + ".coneDir");
        createUniform(uniformName + ".cutoff");
    }

    public void createDirectionalLightUniform(final String uniformName) {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }

    public void createMaterialUniform(final String uniformName) {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".hasTexture");
        createUniform(uniformName + ".hasNormalMap");
        createUniform(uniformName + ".reflectance");
    }

    public void createFogUniform(final String uniformName) {
        createUniform(uniformName + ".isActive");
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".density");
    }

    public void setUniform(final String uniformName, final Matrix4f value) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(
                    this.uniforms.get(uniformName),
                    false,
                    value.get(stack.mallocFloat(16))
            );
        }
    }

    public void setUniform(final String uniformName, final Matrix4f[] matrices) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final int length = matrices != null ? matrices.length : 0;
            final FloatBuffer fb = stack.mallocFloat(16 * length);
            for (int i = 0; i < length; i++) {
                matrices[i].get(16 * i, fb);
            }
            glUniformMatrix4fv(this.uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(final String uniformName, final int value) {
        glUniform1i(this.uniforms.get(uniformName), value);
    }

    public void setUniform(final String uniformName, final float value) {
        glUniform1f(this.uniforms.get(uniformName), value);
    }

    public void setUniform(final String uniformName, final Vector3f value) {
        glUniform3f(this.uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(final String uniformName, final Vector4f value) {
        glUniform4f(this.uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(final String uniformName, final PointLight[] pointLights) {
        final int numLights = pointLights != null ? pointLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            setUniform(uniformName, pointLights[i], i);
        }
    }

    public void setUniform(final String uniformName, final PointLight pointLight, final int pos) {
        setUniform(uniformName + "[" + pos + "]", pointLight);
    }

    public void setUniform(final String uniformName, final PointLight pointLight) {
        setUniform(uniformName + ".colour", pointLight.getColor());
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        final Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }

    public void setUniform(final String uniformName, final SpotLight[] spotLights) {
        final int numLights = spotLights != null ? spotLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            setUniform(uniformName, spotLights[i], i);
        }
    }

    public void setUniform(final String uniformName, final SpotLight spotLight, final int pos) {
        setUniform(uniformName + "[" + pos + "]", spotLight);
    }

    public void setUniform(final String uniformName, final SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".coneDir", spotLight.getConeDirection());
        setUniform(uniformName + ".cutoff", spotLight.getCutOff());
    }

    public void setUniform(final String uniformName, final DirectionalLight dirLight) {
        setUniform(uniformName + ".colour", dirLight.getColor());
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }

    public void setUniform(final String uniformName, final Material material) {
        setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".hasNormalMap", material.hasNormalMap() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    public void setUniform(final String uniformName, final Fog fog) {
        setUniform(uniformName + ".isActive", fog.isActive() ? 1 : 0);
        setUniform(uniformName + ".colour", fog.getColour());
        setUniform(uniformName + ".density", fog.getDensity());
    }

    public void createVertexShader(final String shaderCode) {
        this.vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(final String shaderCode) {
        this.fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(final String shaderCode, final int shaderType) {
        final int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(this.programId, shaderId);

        return shaderId;
    }

    public void link() {
        glLinkProgram(this.programId);
        if (glGetProgrami(this.programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(this.programId, 1024));
        }

        if (this.vertexShaderId != 0) {
            glDetachShader(this.programId, this.vertexShaderId);
        }
        if (this.geometryShaderId != 0) {
            glDetachShader(this.programId, this.geometryShaderId);
        }
        if (this.fragmentShaderId != 0) {
            glDetachShader(this.programId, this.fragmentShaderId);
        }

        glValidateProgram(this.programId);
        if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == 0) {
            LOGGER.error("Warning while validating Shader code: {}", glGetProgramInfoLog(this.programId, 1024));
        }

    }

    public void bind() {
        glUseProgram(this.programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (this.programId != 0) {
            glDeleteProgram(this.programId);
        }
    }
}
