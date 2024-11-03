package com.example.checkweather.service;

import com.example.checkweather.model.CheckWeatherData;
import com.example.checkweather.model.CheckWeatherResponse;
import com.example.checkweather.repository.CheckWeatherRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CheckWeatherService {
    private final CheckWeatherRepository weatherRepository;
    @Qualifier("weatherApiWebClient")
    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(CheckWeatherService.class);


    @Cacheable(cacheNames = "weather", key = "#city + ',' + #country ")
    public Mono<String> getWeatherDescription(String city, String country, String clientApiKey) {
        return weatherRepository.findByCityAndCountry(city, country)
                .flatMap(data -> Mono.just(data.getDescription()))
                .switchIfEmpty(fetchAndCacheWeatherData(city, country, clientApiKey));
    }

    private Mono<String> fetchAndCacheWeatherData(String city, String country, String clientApiKey) {
        // Asynchronously fetch weather data from the downstream API
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", String.format("%s,%s", city, country))
                        .queryParam("appid", clientApiKey)
                        .build())
                .retrieve()
                .bodyToMono(CheckWeatherResponse.class)  // Directly map to WeatherApiResponse
                .flatMap(weatherResponse -> {
                    // Extract the description
                    String description = weatherResponse.getWeather().stream()
                            .findFirst()
                            .map(CheckWeatherResponse.Weather::getDescription)
                            .orElse("No description available");

                    // Save the description asynchronously in H2
                    return saveWeatherData(city, country, description).thenReturn(description);
                });
    }
    @Transactional
    private Mono<Void> saveWeatherData(String city, String country, String description) {
        logger.debug("Saving weather data for city: {}, country: {}, description: {}", city, country, description);
        return weatherRepository.save(CheckWeatherData.builder().city(city).country(country).description(description).build()).then();
    }
}