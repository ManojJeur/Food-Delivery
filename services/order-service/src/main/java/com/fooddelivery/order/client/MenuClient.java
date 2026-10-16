package com.fooddelivery.order.client;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.exception.MenuItemsUnavailableException;
import com.fooddelivery.order.exception.UpstreamServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
@Component
public class MenuClient {
    private final RestClient restClient;
    public MenuClient(@Qualifier("menuRestClient") RestClient restClient) { this.restClient = restClient; }
    public List<MenuItemQuote> quote(List<UUID> ids) {
        try {
            QuoteEnvelope response = restClient.post().uri("/api/v1/menu/items/quote")
                    .body(new QuoteRequest(ids)).retrieve()
                    .body(new ParameterizedTypeReference<QuoteEnvelope>() { });
            if (response == null || response.data() == null) throw new UpstreamServiceUnavailableException("Menu Service");
            return response.data();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409 || exception.getStatusCode().value() == 404)
                throw new MenuItemsUnavailableException();
            throw new UpstreamServiceUnavailableException("Menu Service");
        } catch (RestClientException exception) {
            throw new UpstreamServiceUnavailableException("Menu Service");
        }
    }
    private record QuoteRequest(List<UUID> itemIds) { }
    private record QuoteEnvelope(List<MenuItemQuote> data) { }
}
