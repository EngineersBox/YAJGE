package com.engineersbox.yajge.scene.element.object.md5.primitive;

import com.engineersbox.yajge.util.MD5Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Mesh {

    private static final Pattern PATTERN_SHADER = Pattern.compile("\\s*shader\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PATTERN_VERTEX = Pattern.compile("\\s*vert\\s*(\\d+)\\s*\\(\\s*(" + MD5Utils.FLOAT_REGEXP + ")\\s*(" + MD5Utils.FLOAT_REGEXP + ")\\s*\\)\\s*(\\d+)\\s*(\\d+)");
    private static final Pattern PATTERN_TRI = Pattern.compile("\\s*tri\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)");
    private static final Pattern PATTERN_WEIGHT = Pattern.compile("\\s*weight\\s*(\\d+)\\s*(\\d+)\\s*" + "(" + MD5Utils.FLOAT_REGEXP + ")\\s*" + MD5Utils.VECTOR3_REGEXP );

    private String texture;
    private List<MD5Vertex> vertices;
    private List<MD5Triangle> triangles;
    private List<MD5Weight> weights;

    public MD5Mesh() {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.weights = new ArrayList<>();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("mesh [" + System.lineSeparator());
        str.append("texture: ").append(this.texture).append(System.lineSeparator());

        str.append("vertices [").append(System.lineSeparator());
        for (final MD5Vertex vertex : this.vertices) {
            str.append(vertex).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());

        str.append("triangles [").append(System.lineSeparator());
        for (final MD5Triangle triangle : this.triangles) {
            str.append(triangle).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());

        str.append("weights [").append(System.lineSeparator());
        for (final MD5Weight weight : this.weights) {
            str.append(weight).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());

        return str.toString();
    }

    private static void handleShader(final String line,
                                     final MD5Mesh mesh) {
        final Matcher textureMatcher = PATTERN_SHADER.matcher(line);
        if (textureMatcher.matches()) {
            mesh.setTexture(textureMatcher.group(1));

        }
    }

    private static void handleVertex(final String line,
                                     final List<MD5Vertex> vertices) {
        final Matcher vertexMatcher = PATTERN_VERTEX.matcher(line);
        if (!vertexMatcher.matches()) {
            return;
        }
        final MD5Vertex vertex = new MD5Vertex();
        vertex.setIndex(Integer.parseInt(vertexMatcher.group(1)));
        final float x = Float.parseFloat(vertexMatcher.group(2));
        final float y = Float.parseFloat(vertexMatcher.group(3));
        vertex.setTextCoords(new Vector2f(x, y));
        vertex.setStartWeight(Integer.parseInt(vertexMatcher.group(4)));
        vertex.setWeightCount(Integer.parseInt(vertexMatcher.group(5)));
        vertices.add(vertex);
    }

    private static void handleTriangle(final String line,
                                       final List<MD5Triangle> triangles) {
        final Matcher triMatcher = PATTERN_TRI.matcher(line);
        if (!triMatcher.matches()) {
            return;
        }
        final MD5Triangle triangle = new MD5Triangle();
        triangle.setIndex(Integer.parseInt(triMatcher.group(1)));
        triangle.setVertex0(Integer.parseInt(triMatcher.group(2)));
        triangle.setVertex1(Integer.parseInt(triMatcher.group(3)));
        triangle.setVertex2(Integer.parseInt(triMatcher.group(4)));
        triangles.add(triangle);
    }

    private static void handleWeights(final String line,
                                      final List<MD5Weight> weights) {
        final Matcher weightMatcher = PATTERN_WEIGHT.matcher(line);
        if (!weightMatcher.matches()) {
            return;
        }
        final MD5Weight weight = new MD5Weight();
        weight.setIndex(Integer.parseInt(weightMatcher.group(1)));
        weight.setJointIndex(Integer.parseInt(weightMatcher.group(2)));
        weight.setBias(Float.parseFloat(weightMatcher.group(3)));
        final float x = Float.parseFloat(weightMatcher.group(4));
        final float y = Float.parseFloat(weightMatcher.group(5));
        final float z = Float.parseFloat(weightMatcher.group(6));
        weight.setPosition(new Vector3f(x, y, z));
        weights.add(weight);
    }

    public static MD5Mesh parse(final List<String> meshBlock) {
        final MD5Mesh mesh = new MD5Mesh();
        for (final String line : meshBlock) {
            if (line.contains("shader")) {
                handleShader(line, mesh);
            } else if (line.contains("vert")) {
                handleVertex(line, mesh.getVertices());
            } else if (line.contains("tri")) {
                handleTriangle(line, mesh.getTriangles());
            } else if (line.contains("weight")) {
                handleWeights(line, mesh.getWeights());
            }
        }
        return mesh;
    }

    public String getTexture() {
        return this.texture;
    }

    public void setTexture(final String texture) {
        this.texture = texture;
    }

    public List<MD5Vertex> getVertices() {
        return this.vertices;
    }

    public void setVertices(final List<MD5Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<MD5Triangle> getTriangles() {
        return this.triangles;
    }

    public void setTriangles(final List<MD5Triangle> triangles) {
        this.triangles = triangles;
    }

    public List<MD5Weight> getWeights() {
        return this.weights;
    }

    public void setWeights(final List<MD5Weight> weights) {
        this.weights = weights;
    }

}
