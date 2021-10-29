package com.engineersbox.yajge.scene.element.object.md5.joint;

import java.util.ArrayList;
import java.util.List;

public class MD5JointInfo {

    private List<MD5JointData> joints;

    public List<MD5JointData> getJoints() {
        return this.joints;
    }

    public void setJoints(final List<MD5JointData> joints) {
        this.joints = joints;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("joints [" + System.lineSeparator());
        for (final MD5JointData joint : this.joints) {
            str.append(joint).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());
        return str.toString();
    }

    public static MD5JointInfo parse(final List<String> blockBody) {
        final MD5JointInfo result = new MD5JointInfo();
        final List<MD5JointData> joints = new ArrayList<>();
        for (final String line : blockBody) {
            final MD5JointData jointData = MD5JointData.parseLine(line);
            if (jointData != null) {
                joints.add(jointData);
            }
        }
        result.setJoints(joints);
        return result;
    }

}
