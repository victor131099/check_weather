package com.example.check_weather_api.utils;

import com.example.check_weather_api.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    void enforceRateLimit_WithinLimit_ShouldNotThrowException() {
        String clientApiKey = "testClient";

        // Make 5 requests within limit, no exception should be thrown
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.enforceRateLimit(clientApiKey));
        }
    }

    @Test
    void enforceRateLimit_ExceedLimit_ShouldThrowRateLimitExceededException() {
        String clientApiKey = "testClient";

        // Make 5 requests within limit
        for (int i = 0; i < 5; i++) {
            rateLimiter.enforceRateLimit(clientApiKey);
        }

        // Sixth request should exceed the limit and throw RateLimitExceededException
        assertThrows(RateLimitExceededException.class, () -> rateLimiter.enforceRateLimit(clientApiKey));
    }

    @Test
    void enforceRateLimit_AfterReset_ShouldAllowRequests() {
        String clientApiKey = "testClient";

        // Make 5 requests within limit
        for (int i = 0; i < 5; i++) {
            rateLimiter.enforceRateLimit(clientApiKey);
        }

        // Simulate time passing by setting firstRequestTime to more than an hour ago
        rateLimiter.getFirstRequestTime().minus(1, ChronoUnit.HOURS);

        // After reset, 5 more requests should be allowed without throwing an exception
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.enforceRateLimit(clientApiKey));
        }
    }

    @Test
    void reset_ShouldResetRequestCountAndFirstRequestTime() {
        String clientApiKey = "testClient";

        // Make 3 requests to increment the count
        for (int i = 0; i < 3; i++) {
            rateLimiter.enforceRateLimit(clientApiKey);
        }

        // Capture the firstRequestTime before reset
        LocalDateTime firstRequestTimeBeforeReset = rateLimiter.getFirstRequestTime();

        // Reset the rate limiter
        rateLimiter.reset();

        // Ensure request count is reset to 0 and firstRequestTime is updated
        assertEquals(0, rateLimiter.getRequestCount().get());
        assertNotEquals(firstRequestTimeBeforeReset, rateLimiter.getFirstRequestTime());
    }
}
