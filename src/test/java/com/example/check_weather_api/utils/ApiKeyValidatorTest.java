package com.example.check_weather_api.utils;

import com.example.check_weather_api.configuration.ApiKeyConfig;
import com.example.check_weather_api.exception.InvalidApiKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ApiKeyValidatorTest {

    @Mock
    private ApiKeyConfig apiKeyConfig;

    @InjectMocks
    private ApiKeyValidator apiKeyValidator;

    private final List<String> validKeys = Arrays.asList("validApiKey1", "validApiKey2");

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        // Mocking the getKeys method to return a predefined list of valid API keys
        when(apiKeyConfig.getKeys()).thenReturn(validKeys);
    }

    @Test
    void validate_NullOrEmptyApiKey_ShouldThrowInvalidApiKeyException() {
        List<String> invalidKeys = Arrays.asList(null, "");

        invalidKeys.forEach(apiKey -> {
            InvalidApiKeyException exception = assertThrows(InvalidApiKeyException.class, () ->
                    apiKeyValidator.validate(apiKey)
            );
            assertEquals("Missing API key.", exception.getMessage());
        });
    }

    @Test
    void validate_InvalidApiKey_ShouldThrowInvalidApiKeyException() {
        InvalidApiKeyException exception = assertThrows(InvalidApiKeyException.class, () ->
                apiKeyValidator.validate("invalidApiKey")
        );
        assertEquals("Invalid API key.", exception.getMessage());
    }

    @Test
    void validate_ValidApiKey_ShouldNotThrowException() {
        // Using a valid API key from the mocked list
        assertDoesNotThrow(() -> apiKeyValidator.validate("validApiKey1"));
    }
}
