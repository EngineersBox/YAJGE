package com.engineersbox.yajge.scene.element.object.md5.frame;

import com.engineersbox.yajge.util.MD5Utils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5BaseFrameData {

    private static final Pattern PATTERN_BASEFRAME = Pattern.compile("\\s*" + MD5Utils.VECTOR3_REGEXP + "\\s*" + MD5Utils.VECTOR3_REGEXP + ".*");

    private Vector3f position;
    private Quaternionf orientation;

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public Quaternionf getOrientation() {
        return this.orientation;
    }

    public void setOrientation(final Vector3f vec) {
        this.orientation = MD5Utils.calculateQuaternion(vec);
    }

    @Override
    public String toString() {
        return "[position: " + this.position + ", orientation: " + this.orientation + "]";
    }

    public static MD5BaseFrameData parseLine(final String line) {
        final Matcher matcher = PATTERN_BASEFRAME.matcher(line);
        MD5BaseFrameData result = null;
        if (matcher.matches()) {
            result = new MD5BaseFrameData();
            float x = Float.parseFloat(matcher.group(1));
            float y = Float.parseFloat(matcher.group(2));
            float z = Float.parseFloat(matcher.group(3));
            result.setPosition(new Vector3f(x, y, z));

            x = Float.parseFloat(matcher.group(4));
            y = Float.parseFloat(matcher.group(5));
            z = Float.parseFloat(matcher.group(6));
            result.setOrientation(new Vector3f(x, y, z));
        }

        return result;
    }
}
