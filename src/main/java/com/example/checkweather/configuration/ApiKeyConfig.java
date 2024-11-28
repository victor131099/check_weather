package com.example.checkweather.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "openweathermap.api")
public class ApiKeyConfig {
    private List<String> keys;
    private Client client;

    @Getter
    @Setter
    public static class Client {
        private String baseUrl;
        private int connTimeout;
        private int readTimeout;
        private int writeTimeout;
        private int responseTimeout;
    }
}
