package com.devhub.devhub.article;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.parser")
public record ParserProperties(List<Source> sources) {

    public ParserProperties {
        if (sources == null) {
            sources = List.of();
        }
    }

    public record Source(String name, String url) {}
}
