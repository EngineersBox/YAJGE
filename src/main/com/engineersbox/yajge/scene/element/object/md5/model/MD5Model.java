package com.engineersbox.yajge.scene.element.object.md5.model;

import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.scene.element.object.md5.joint.MD5JointInfo;
import com.engineersbox.yajge.scene.element.object.md5.primitive.MD5Mesh;

import java.util.ArrayList;
import java.util.List;

public class MD5Model {

    private MD5JointInfo jointInfo;
    private MD5ModelHeader header;
    private List<MD5Mesh> meshes;

    public MD5Model() {
        this.meshes = new ArrayList<>();
    }
    
    public MD5JointInfo getJointInfo() {
        return this.jointInfo;
    }

    public void setJointInfo(final MD5JointInfo jointInfo) {
        this.jointInfo = jointInfo;
    }

    public MD5ModelHeader getHeader() {
        return this.header;
    }

    public void setHeader(final MD5ModelHeader header) {
        this.header = header;
    }

    public List<MD5Mesh> getMeshes() {
        return this.meshes;
    }

    public void setMeshes(final List<MD5Mesh> meshes) {
        this.meshes = meshes;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("MD5MeshModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getJointInfo()).append(System.lineSeparator());
        
        for (final MD5Mesh mesh : this.meshes) {
            str.append(mesh).append(System.lineSeparator());
        }
        return str.toString();
    }

    public static MD5Model parse(final String meshModelFile) {
        final List<String> lines = ResourceLoader.loadAsStringLines(meshModelFile);

        final MD5Model result = new MD5Model();

        if (lines.isEmpty()) {
            throw new RuntimeException("Cannot parse empty file");
        }

        // Parse Header
        boolean headerEnd = false;
        int start = 0;
        for (int i = 0; i < lines.size() && !headerEnd; i++) {
            final String line = lines.get(i);
            headerEnd = line.trim().endsWith("{");
            start = i;
        }
        if (!headerEnd) {
            throw new RuntimeException("Cannot find header");
        }
        final List<String> headerBlock = lines.subList(0, start);
        final MD5ModelHeader header = MD5ModelHeader.parse(headerBlock);
        result.setHeader(header);

        // Parse the rest of block
        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                final List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }

        return result;
    }

    private static void parseBlock(final MD5Model model, final String blockId, final List<String> blockBody) {
        switch (blockId) {
            case "joints" -> {
                final MD5JointInfo jointInfo = MD5JointInfo.parse(blockBody);
                model.setJointInfo(jointInfo);
            }
            case "mesh" -> {
                final MD5Mesh md5Mesh = MD5Mesh.parse(blockBody);
                model.getMeshes().add(md5Mesh);
            }
            default -> {
            }
        }
    }

}
