package com.engineersbox.yajge.resources.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "render")
@XmlAccessorType(XmlAccessType.FIELD)
public class RenderConfig extends VersionedConfig {

    @XmlElement(defaultValue = "60.0f")
    public float fov;

    @XmlElement(defaultValue = "0.01f")
    public float zNear;

    @XmlElement(defaultValue = "1000.0f")
    public float zFar;

}
