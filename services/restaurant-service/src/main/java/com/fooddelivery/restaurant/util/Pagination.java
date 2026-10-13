package com.fooddelivery.restaurant.util;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pagination {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt", "updatedAt");
    private static final int MAX_PAGE_SIZE = 50;

    private Pagination() {
    }

    public static Pageable of(int page, int size, String requestedSort) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        String[] sortParts = requestedSort == null ? new String[0] : requestedSort.split(",", -1);
        if (sortParts.length < 1 || sortParts.length > 2 || !ALLOWED_SORT_FIELDS.contains(sortParts[0])) {
            throw new IllegalArgumentException("sort must use name, createdAt, or updatedAt");
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length == 2) {
            try {
                direction = Sort.Direction.valueOf(sortParts[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("sort direction must be asc or desc");
            }
        }
        return PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
    }
}
