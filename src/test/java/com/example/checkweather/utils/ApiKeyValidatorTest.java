package com.example.checkweather.utils;

import com.example.checkweather.configuration.ApiKeyConfig;
import com.example.checkweather.exception.InvalidApiKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiKeyValidatorTest {

    private ApiKeyValidator apiKeyValidator;

    private final List<String> validKeys = Arrays.asList("validApiKey1", "validApiKey2");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a spy of ApiKeyConfig and configure it to return validKeys
        ApiKeyConfig apiKeyConfigSpy = spy(new ApiKeyConfig());
        doReturn(validKeys).when(apiKeyConfigSpy).getKeys();

        // Manually instantiate ApiKeyValidator with the spy
        apiKeyValidator = new ApiKeyValidator(apiKeyConfigSpy);
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
        // Test with a valid API key, expecting no exceptions to be thrown
        assertDoesNotThrow(() -> apiKeyValidator.validate("validApiKey1"));
    }
}

