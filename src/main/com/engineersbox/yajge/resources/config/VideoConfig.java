package com.engineersbox.yajge.resources.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "video")
@XmlAccessorType(XmlAccessType.FIELD)
public class VideoConfig extends VersionedConfig {

    @XmlElement(defaultValue = "60")
    public int fps;

    @XmlElement(defaultValue = "30")
    public int ups;

}
