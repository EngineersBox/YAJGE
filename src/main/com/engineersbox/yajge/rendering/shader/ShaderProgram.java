package com.engineersbox.yajge.rendering.shader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private static final Logger LOGGER = LogManager.getLogger(ShaderProgram.class);

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() throws Exception {
        this.programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader"); // TODO: Implement an exception for this
        }
    }

    public void createVertexShader(final String shaderCode) throws Exception {
        this.vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(final String shaderCode) throws Exception {
        this.fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(final String shaderCode, final int shaderType) throws Exception {
        final int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType); // TODO: Implement an exception for this
        }
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024)); // TODO: Implement an exception for this
        }
        glAttachShader(this.programId, shaderId);
        LOGGER.debug("Created shader {} [Type: {}]", shaderId, shaderType);
        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(this.programId);
        if (glGetProgrami(this.programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(this.programId, 1024)); // TODO: Implement an exception for this
        }
        if (this.vertexShaderId != 0) {
            glDetachShader(this.programId, this.vertexShaderId);
        }
        if (this.fragmentShaderId != 0) {
            glDetachShader(this.programId, this.fragmentShaderId);
        }
        glValidateProgram(this.programId);
        if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == 0) {
            LOGGER.warn("Warning while validating shader: {}", glGetProgramInfoLog(this.programId, 1024));
        }
        LOGGER.trace("Linked shader {}", this.programId);
    }

    public void bind() {
        glUseProgram(this.programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        LOGGER.trace("Unbound shader {}", this.programId);
        if (this.programId != 0) {
            glDeleteProgram(this.programId);
            LOGGER.trace("Removed shader {}", this.programId);
        }
    }
}
