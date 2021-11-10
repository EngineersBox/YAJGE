package com.engineersbox.yajge.resources.loader.assimp;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private final List<Node> children;
    private final String name;
    private final Node parent;
    private final Matrix4f nodeTransform;

    public Node(final String name,
                final Node parent,
                final Matrix4f nodeTransform) {
        this.name = name;
        this.parent = parent;
        this.nodeTransform = nodeTransform;
        this.children = new ArrayList<>();
    }

    public Matrix4f getNodeTransform() {
        return this.nodeTransform;
    }

    public void addChild(final Node node) {
        this.children.add(node);
    }

    public List<Node> getChildren() {
        return this.children;
    }

    public String getName() {
        return this.name;
    }

    public Node getParent() {
        return this.parent;
    }
}
