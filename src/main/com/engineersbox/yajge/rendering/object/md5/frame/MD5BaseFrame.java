package com.engineersbox.yajge.rendering.object.md5.frame;

import java.util.ArrayList;
import java.util.List;

public class MD5BaseFrame {

    private List<MD5BaseFrameData> frameDataList;
    public List<MD5BaseFrameData> getFrameDataList() {
        return this.frameDataList;
    }

    public void setFrameDataList(final List<MD5BaseFrameData> frameDataList) {
        this.frameDataList = frameDataList;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("base frame [" + System.lineSeparator());
        for (final MD5BaseFrameData frameData : this.frameDataList) {
            str.append(frameData).append(System.lineSeparator());
        }
        str.append(']').append(System.lineSeparator());
        return str.toString();
    }

    public static MD5BaseFrame parse(final List<String> blockBody) {
        final MD5BaseFrame result = new MD5BaseFrame();

        final List<MD5BaseFrameData> frameInfoList = new ArrayList<>();
        result.setFrameDataList(frameInfoList);

        for (final String line : blockBody) {
            final MD5BaseFrameData frameInfo = MD5BaseFrameData.parseLine(line);
            if (frameInfo != null) {
                frameInfoList.add(frameInfo);
            }
        }

        return result;
    }

}
