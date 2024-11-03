package com.example.checkweather.repository;

import com.example.checkweather.model.CheckWeatherData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CheckWeatherRepository extends ReactiveCrudRepository<CheckWeatherData, Long> {
    Mono<CheckWeatherData> findByCityAndCountry(String city, String country);
}
