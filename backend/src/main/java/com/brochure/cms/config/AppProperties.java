package com.brochure.cms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String baseUrl, MediaProperties media, MailProperties mail) {

    public record MediaProperties(String root) {}

    public record MailProperties(String from) {}
}
