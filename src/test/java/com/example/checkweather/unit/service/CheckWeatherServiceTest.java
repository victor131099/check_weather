package service;

import com.example.checkweather.model.CheckWeatherData;
import com.example.checkweather.model.CheckWeatherResponse;
import com.example.checkweather.repository.CheckWeatherRepository;
import com.example.checkweather.service.CheckWeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckWeatherServiceTest {

    @Mock
    private CheckWeatherRepository weatherRepository;

    @Mock
    @Qualifier("weatherApiWebClient")
    private WebClient webClient;

    @InjectMocks
    private CheckWeatherService checkWeatherService;

    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock;
    private WebClient.RequestHeadersSpec<?> requestHeadersSpecMock;
    private WebClient.ResponseSpec responseSpecMock;

    @BeforeEach
    void setup() {
        requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        responseSpecMock = mock(WebClient.ResponseSpec.class);

        // Set up WebClient mock chain with lenient stubbings
        lenient().doReturn(requestHeadersUriSpecMock).when(webClient).get();
        lenient().doReturn(requestHeadersSpecMock).when(requestHeadersUriSpecMock).uri(any(Function.class));
        lenient().doReturn(responseSpecMock).when(requestHeadersSpecMock).retrieve();

        // Mock bodyToMono to return a valid Mono<CheckWeatherResponse> with lenient stubbing
        CheckWeatherResponse.Weather weather = new CheckWeatherResponse.Weather();
        weather.setDescription("Sunny");
        CheckWeatherResponse weatherResponse = new CheckWeatherResponse();
        weatherResponse.setWeather(List.of(weather));

        lenient().doReturn(Mono.just(weatherResponse)).when(responseSpecMock).bodyToMono(CheckWeatherResponse.class);
    }

    @Test
    void getWeatherDescription_whenDataExistsInRepository_shouldReturnDescription() {
        String city = "Sydney";
        String country = "Australia";
        String description = "Clear sky";
        String clientApiKey = "testApiKey";

        CheckWeatherData data = CheckWeatherData.builder()
                .city(city)
                .country(country)
                .description(description)
                .build();

        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Mono.just(data));

        StepVerifier.create(checkWeatherService.getWeatherDescription(city, country, clientApiKey))
                .expectNext(description)
                .verifyComplete();

        verify(weatherRepository, times(1)).findByCityAndCountry(city, country);
    }

    @Test
    void getWeatherDescription_whenApiReturnsError_shouldReturnError() {
        String city = "Sydney";
        String country = "Australia";
        String clientApiKey = "testApiKey";

        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Mono.empty());
        doReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)))
                .when(responseSpecMock).bodyToMono(CheckWeatherResponse.class);

        StepVerifier.create(checkWeatherService.getWeatherDescription(city, country, clientApiKey))
                .expectError(WebClientResponseException.class)
                .verify();

        verify(weatherRepository, times(1)).findByCityAndCountry(city, country);
        verify(webClient, times(1)).get();
    }

    @Test
    void fetchWeatherData_whenDataNotInRepository_shouldFetchFromExternalApi() {
        String city = "Sydney";
        String country = "Australia";
        String clientApiKey = "testApiKey";
        String description = "Sunny";

        // Ensure repository returns empty to simulate cache miss
        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Mono.empty());

        // Mock the save method to return Mono.empty(), simulating a successful save operation
        when(weatherRepository.save(any(CheckWeatherData.class))).thenReturn(Mono.empty());

        // Capture the URI function for verification
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunctionCaptor = ArgumentCaptor.forClass(Function.class);
        doReturn(requestHeadersSpecMock).when(requestHeadersUriSpecMock).uri(uriFunctionCaptor.capture());
        doReturn(responseSpecMock).when(requestHeadersSpecMock).retrieve();

        // Mock the WebClient response to return expected weather data
        CheckWeatherResponse.Weather weather = new CheckWeatherResponse.Weather();
        weather.setDescription(description);
        CheckWeatherResponse weatherResponse = new CheckWeatherResponse();
        weatherResponse.setWeather(List.of(weather));

        doReturn(Mono.just(weatherResponse)).when(responseSpecMock).bodyToMono(CheckWeatherResponse.class);

        // Invoke the service method and verify the expected output
        StepVerifier.create(checkWeatherService.getWeatherDescription(city, country, clientApiKey))
                .expectNext(description)
                .verifyComplete();

        // Verify URI construction and WebClient interaction
        verify(requestHeadersUriSpecMock).uri(any(Function.class));

        // Verify that the correct URI was generated
        Function<UriBuilder, URI> capturedUriFunction = uriFunctionCaptor.getValue();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        UriBuilder uriBuilder = factory.builder();
        URI generatedUri = capturedUriFunction.apply(uriBuilder);
        assertEquals(String.format("?q=%s,%s&appid=%s", city, country, clientApiKey), generatedUri.toString());

        // Confirm WebClient was called and repository save was invoked
        verify(webClient, times(1)).get();
        verify(responseSpecMock).bodyToMono(CheckWeatherResponse.class);
        verify(weatherRepository, times(1)).save(any(CheckWeatherData.class));
    }

    @Test
    void saveWeatherData_whenFetchedDataIsPresent_shouldSaveToRepository() {
        String city = "Sydney";
        String country = "Australia";
        String clientApiKey = "testApiKey";
        String description = "Sunny";

        // Configure repository to return empty, simulating a cache miss
        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Mono.empty());

        // Mock the external API response
        CheckWeatherResponse.Weather weather = new CheckWeatherResponse.Weather();
        weather.setDescription(description);
        CheckWeatherResponse weatherResponse = new CheckWeatherResponse();
        weatherResponse.setWeather(List.of(weather));

        // Configure the WebClient mock chain to return the expected weather data
        doReturn(Mono.just(weatherResponse)).when(responseSpecMock).bodyToMono(CheckWeatherResponse.class);

        // Configure repository to simulate saving data
        when(weatherRepository.save(any(CheckWeatherData.class))).thenReturn(Mono.empty());

        // Invoke the method
        StepVerifier.create(checkWeatherService.getWeatherDescription(city, country, clientApiKey))
                .expectNext(description)
                .verifyComplete();

        // Verify that the data was saved to the repository
        verify(weatherRepository, times(1)).save(argThat(savedData ->
                savedData.getCity().equals(city) &&
                        savedData.getCountry().equals(country) &&
                        savedData.getDescription().equals(description)
        ));

        // No additional verification for WebClient behavior here
        verify(webClient, times(1)).get();
    }

}




