package com.example.checkweather.service;

import com.example.checkweather.model.CheckWeatherData;
import com.example.checkweather.model.CheckWeatherResponse;
import com.example.checkweather.repository.CheckWeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

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

        // Set up WebClient mock chain
        doReturn(requestHeadersUriSpecMock).when(webClient).get();
        doReturn(requestHeadersSpecMock).when(requestHeadersUriSpecMock).uri(any(Function.class));
        doReturn(responseSpecMock).when(requestHeadersSpecMock).retrieve();

        // Mark bodyToMono stub as lenient
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
    void getWeatherDescription_whenDataNotInRepository_shouldFetchAndCacheData() {
        String city = "Sydney";
        String country = "Australia";
        String clientApiKey = "testApiKey";
        String description = "Sunny";  // Match the mocked weather description

        when(weatherRepository.findByCityAndCountry(city, country)).thenReturn(Mono.empty());
        when(weatherRepository.save(any(CheckWeatherData.class))).thenReturn(Mono.empty());

        StepVerifier.create(checkWeatherService.getWeatherDescription(city, country, clientApiKey))
                .expectNext(description)
                .verifyComplete();

        verify(weatherRepository, times(1)).findByCityAndCountry(city, country);
        verify(weatherRepository, times(1)).save(any(CheckWeatherData.class));
        verify(webClient, times(1)).get();
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
}




