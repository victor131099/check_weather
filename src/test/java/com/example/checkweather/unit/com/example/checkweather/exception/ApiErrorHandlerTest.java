package com.example.checkweather.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorHandlerTest {

    @InjectMocks
    private ApiErrorHandler apiErrorHandler;

    private static final String CITY = "New York";
    private static final String COUNTRY = "US";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleApiError_WithWebClientResponseException_ReturnsCorrectStatusAndMessage() {
        String errorMessage = "Not Found";
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                null,
                errorMessage.getBytes(),
                null
        );

        Mono<ResponseEntity<String>> result = apiErrorHandler.handleApiError(exception, CITY, COUNTRY);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertEquals(errorMessage, response.getBody());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testHandleApiError_WithEmptyResponseBody_ReturnsReasonPhrase() {
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                null,
                new byte[0], // Empty response body
                null
        );

        Mono<ResponseEntity<String>> result = apiErrorHandler.handleApiError(exception, CITY, COUNTRY);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Error: Bad Request", response.getBody());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testHandleApiError_WithUnknownException_ReturnsInternalServerError() {
        Exception unknownException = new RuntimeException("Unexpected error");

        Mono<ResponseEntity<String>> result = apiErrorHandler.handleApiError(unknownException, CITY, COUNTRY);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("Service Unavailable", response.getBody());
                    return true;
                })
                .verifyComplete();
    }
}
