package com.fooddelivery.order.client;
import java.util.UUID;
import com.fooddelivery.order.exception.RestaurantNotFoundException;
import com.fooddelivery.order.exception.RestaurantUnavailableException;
import com.fooddelivery.order.exception.UpstreamServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
@Component
public class RestaurantClient {
    private final RestClient restClient;
    public RestaurantClient(@Qualifier("restaurantRestClient") RestClient restClient) { this.restClient = restClient; }
    public void requireOpen(UUID restaurantId) {
        try {
            RestaurantEnvelope response = restClient.get().uri("/api/v1/restaurants/{id}", restaurantId)
                    .retrieve().body(new ParameterizedTypeReference<RestaurantEnvelope>() { });
            if (response == null || response.data() == null) throw new UpstreamServiceUnavailableException("Restaurant Service");
            if (!"OPEN".equals(response.data().status())) throw new RestaurantUnavailableException();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new RestaurantNotFoundException(restaurantId);
        } catch (RestClientResponseException exception) {
            throw new UpstreamServiceUnavailableException("Restaurant Service");
        } catch (RestClientException exception) {
            throw new UpstreamServiceUnavailableException("Restaurant Service");
        }
    }
    public void assertCanManage(UUID restaurantId, String token) {
        try {
            restClient.get().uri("/api/v1/restaurants/{id}/management-access", restaurantId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new com.fooddelivery.order.exception.UnauthorizedOperationException();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new RestaurantNotFoundException(restaurantId);
        } catch (RestClientException exception) {
            throw new UpstreamServiceUnavailableException("Restaurant Service");
        }
    }
    private record RestaurantData(String status) { }
    private record RestaurantEnvelope(RestaurantData data) { }
}
