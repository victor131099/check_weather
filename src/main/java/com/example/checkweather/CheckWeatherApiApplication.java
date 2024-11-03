package com.example.checkweather;

import com.example.checkweather.configuration.ApiKeyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(ApiKeyConfig.class)
public class CheckWeatherApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckWeatherApiApplication.class, args);
	}

}
