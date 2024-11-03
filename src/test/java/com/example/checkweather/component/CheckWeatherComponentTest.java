import com.example.checkweather.CheckWeatherApiApplication;
import com.example.checkweather.exception.InvalidApiKeyException;
import com.example.checkweather.exception.RateLimitExceededException;
import com.example.checkweather.service.CheckWeatherService;
import com.example.checkweather.utils.ApiKeyValidator;
import com.example.checkweather.utils.RateLimiter;
import com.example.checkweather.exception.ApiErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = CheckWeatherApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckWeatherComponentTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CheckWeatherService checkWeatherService;

    @MockBean
    private ApiErrorHandler apiErrorHandler;

    @MockBean
    private ApiKeyValidator apiKeyValidator;

    @MockBean
    private RateLimiter rateLimiter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String CITY = "New York";
    private static final String COUNTRY = "US";
    private static final String API_URL = "/api/weather";

    private static final String API_KEY = "dummyApiKey";

    @Test
    void testGetWeatherDescription_Success() {
        String city = "New York";
        String apiKey = "validApiKey";
        String weatherDescription = "Sunny";

        when(checkWeatherService.getWeatherDescription(city, null, apiKey))
                .thenReturn(Mono.just(weatherDescription));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/weather")
                        .queryParam("city", city)
                        .queryParam("apiKey", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(weatherDescription);
    }

    @Test
    void testGetWeatherDescription_InvalidApiKey() {
        String city = "New York";
        String apiKey = "invalidApiKey";

        doThrow(new InvalidApiKeyException("Invalid API Key"))
                .when(apiKeyValidator).validate(apiKey);

        when(checkWeatherService.getWeatherDescription(city, null, apiKey))
                .thenReturn(Mono.just(""));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/weather")
                        .queryParam("city", city)
                        .queryParam("apiKey", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class).isEqualTo("Error: Invalid API key provided. Please check your API key and try again.");
    }

    @Test
    void testGetWeatherDescription_RateLimitExceeded() {
        String city = "New York";
        String apiKey = "validApiKey";

        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimiter).enforceRateLimit(apiKey);

        when(checkWeatherService.getWeatherDescription(city, null, apiKey))
                .thenReturn(Mono.just(""));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/weather")
                        .queryParam("city", city)
                        .queryParam("apiKey", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody(String.class).isEqualTo("Error: API rate limit exceeded. Please try again later.");
    }

    @Test
    public void testGetWeatherDescription_ApiError_Returns503ServiceUnavailable() {
        // Mock a WebClientResponseException with a 503 status
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                null,
                null,
                null
        );

        // Mock the CheckWeatherService to throw the exception
        when(checkWeatherService.getWeatherDescription(CITY, COUNTRY, API_KEY))
                .thenReturn(Mono.error(exception));

        // Mock the ApiErrorHandler to handle the exception and return 503
        when(apiErrorHandler.handleApiError(exception, CITY, COUNTRY))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable")));

        // Perform the test
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("city", CITY)
                        .queryParam("country", COUNTRY)
                        .queryParam("apiKey", API_KEY)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("Service Unavailable");
    }

    @Test
    public void testGetWeatherDescription_UnknownError_Returns503ServiceUnavailable() {
        // Simulate an unknown error
        Throwable unknownError = new RuntimeException("Unknown error");

        // Mock the CheckWeatherService to throw an unknown exception
        when(checkWeatherService.getWeatherDescription(CITY, COUNTRY, API_KEY))
                .thenReturn(Mono.error(unknownError));

        // Mock the ApiErrorHandler to handle the unknown exception and return 503
        when(apiErrorHandler.handleApiError(unknownError, CITY, COUNTRY))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable")));

        // Perform the test
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(API_URL)
                        .queryParam("city", CITY)
                        .queryParam("country", COUNTRY)
                        .queryParam("apiKey", API_KEY)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class).isEqualTo("Service Unavailable");
    }
}
