package com.pd.ecommerce.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class OpenTelemetryLoggingConfig implements InitializingBean {

	private final OpenTelemetry openTelemetry;


	public OpenTelemetryLoggingConfig(OpenTelemetry openTelemetry) {
		this.openTelemetry = openTelemetry;
	}

	@Override
	public void afterPropertiesSet() {
		OpenTelemetryAppender.install(openTelemetry);
	}
}