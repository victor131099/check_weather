package com.example.checkweather.utils;

import com.example.checkweather.configuration.ApiKeyConfig;
import com.example.checkweather.exception.InvalidApiKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ApiKeyValidator {
    private final List<String> validApiKeys;

    @Autowired
    public ApiKeyValidator(ApiKeyConfig apiKeyConfig) {
        this.validApiKeys = apiKeyConfig.getKeys(); // Retrieve the valid API keys from ApiKeyConfig
    }

    public void validate(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new InvalidApiKeyException("Missing API key.");
        }

        if (!validApiKeys.contains(apiKey)) {
            throw new InvalidApiKeyException("Invalid API key.");
        }
    }
}
