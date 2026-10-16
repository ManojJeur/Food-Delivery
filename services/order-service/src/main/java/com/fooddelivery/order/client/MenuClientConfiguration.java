package com.fooddelivery.order.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
@Configuration
public class MenuClientConfiguration {
    @Bean("menuRestClient")
    public RestClient menuRestClient(RestClient.Builder builder, @Value("${service-clients.menu-url}") String url) {
        return builder.baseUrl(url).build();
    }
    @Bean("restaurantRestClient")
    public RestClient restaurantRestClient(RestClient.Builder builder, @Value("${service-clients.restaurant-url}") String url) {
        return builder.baseUrl(url).build();
    }
}
