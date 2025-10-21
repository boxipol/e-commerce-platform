package com.pdp.ecommerce.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "HelloRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class HelloRequest {

	private String name;
}