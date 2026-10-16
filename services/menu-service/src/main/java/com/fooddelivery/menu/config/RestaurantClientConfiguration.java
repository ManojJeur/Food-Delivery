package com.fooddelivery.menu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestaurantClientConfiguration {
    @Bean
    public RestClient restaurantRestClient(RestClient.Builder builder,
            @Value("${restaurant-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
