package com.engineersbox.yajge.resources.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class VersionedConfig {
    @XmlAttribute(required = true)
    public int version;
}
