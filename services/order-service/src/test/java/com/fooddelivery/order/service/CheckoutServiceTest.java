package com.fooddelivery.order.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import com.fooddelivery.order.client.MenuClient;
import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.exception.IdempotencyKeyConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
    @Mock private CheckoutTransactionService transactions;
    @Mock private CartMutationService carts;
    @Mock private MenuClient menu;
    @Mock private RestaurantClient restaurants;
    @Mock private OrderQueryService queries;
    private CheckoutService service;
    @BeforeEach void setUp() { service = new CheckoutService(transactions, carts, menu, restaurants, queries); }
    @Test void retryWithSameKeyAndAddressReturnsOriginalOrderWithoutTouchingCart() throws Exception {
        UUID customer = UUID.randomUUID(); UUID orderId = UUID.randomUUID(); String address = "123 Main Street";
        when(transactions.findIdempotency(customer, "key-7")).thenReturn(new CheckoutTransactionService.CheckoutLookup(hash(address), orderId));
        OrderResponse response = new OrderResponse(orderId, customer, UUID.randomUUID(), address, null, null, null, null,
                null, java.util.List.of(), java.util.List.of(), null, null);
        when(queries.getCustomerOrder(customer, orderId)).thenReturn(response);

        assertEquals(response, service.checkout(customer, " key-7 ", address));

        verify(queries).getCustomerOrder(customer, orderId);
        verify(carts, never()).snapshotForCheckout(customer);
        verify(menu, never()).quote(org.mockito.ArgumentMatchers.anyList());
    }
    @Test void retryWithSameKeyAndDifferentAddressIsRejected() throws Exception {
        UUID customer = UUID.randomUUID();
        when(transactions.findIdempotency(customer, "key-7")).thenReturn(
                new CheckoutTransactionService.CheckoutLookup(hash("old address"), UUID.randomUUID()));
        assertThrows(IdempotencyKeyConflictException.class,
                () -> service.checkout(customer, "key-7", "new address"));
    }
    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
