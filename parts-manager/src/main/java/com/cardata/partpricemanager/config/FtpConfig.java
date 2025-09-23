package com.cardata.partpricemanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(FtpProperties.class)
public class FtpConfig {}