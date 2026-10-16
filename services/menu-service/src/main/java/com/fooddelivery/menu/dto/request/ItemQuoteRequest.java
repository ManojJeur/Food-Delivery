package com.fooddelivery.menu.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemQuoteRequest(@NotEmpty @Size(max = 100) List<@NotNull UUID> itemIds) {
}
