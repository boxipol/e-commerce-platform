package com.bbb.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "HelloResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class HelloResponse {

	private String greeting;
}