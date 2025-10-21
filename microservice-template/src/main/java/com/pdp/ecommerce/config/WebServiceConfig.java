package com.pdp.ecommerce.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig {

	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);

		return new ServletRegistrationBean<>(servlet, "/ws/*");
	}

	@Bean(name = "hello")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema helloSchema) {
		DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
		definition.setPortTypeName("HelloPort");
		definition.setLocationUri("/ws");
		definition.setTargetNamespace("http://example.com/demo");
		definition.setSchema(helloSchema);

		return definition;
	}

	@Bean
	public XsdSchema helloSchema() {
		return new SimpleXsdSchema(new ClassPathResource("hello.xsd"));
	}
}