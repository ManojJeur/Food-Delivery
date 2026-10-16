package com.fooddelivery.menu.client;

import java.util.UUID;

import com.fooddelivery.menu.exception.RestaurantNotFoundException;
import com.fooddelivery.menu.exception.RestaurantServiceUnavailableException;
import com.fooddelivery.menu.exception.UnauthorizedOperationException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestaurantAccessClient {
    private final RestClient restaurantRestClient;

    public RestaurantAccessClient(RestClient restaurantRestClient) {
        this.restaurantRestClient = restaurantRestClient;
    }

    public void assertCanManage(UUID restaurantId, String accessToken) {
        try {
            restaurantRestClient.get()
                    .uri("/api/v1/restaurants/{restaurantId}/management-access", restaurantId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new UnauthorizedOperationException();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new RestaurantNotFoundException(restaurantId);
        } catch (RestClientException exception) {
            throw new RestaurantServiceUnavailableException();
        }
    }
}
