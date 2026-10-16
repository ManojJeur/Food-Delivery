package com.fooddelivery.order.util;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
public final class Pagination {
    private static final Set<String> FIELDS = Set.of("createdAt", "updatedAt", "totalAmount", "status");
    private Pagination() { }
    public static Pageable of(int page, int size, String requestedSort) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1 || size > 50) throw new IllegalArgumentException("size must be between 1 and 50");
        String[] parts = requestedSort == null ? new String[0] : requestedSort.split(",", -1);
        if (parts.length < 1 || parts.length > 2 || !FIELDS.contains(parts[0])) throw new IllegalArgumentException("sort must use createdAt, updatedAt, totalAmount, or status");
        Sort.Direction dir = Sort.Direction.DESC;
        if (parts.length == 2) {
            try { dir = Sort.Direction.valueOf(parts[1].toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("sort direction must be asc or desc"); }
        }
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
