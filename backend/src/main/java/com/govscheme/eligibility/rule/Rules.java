package com.govscheme.eligibility.rule;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared helpers: CSV parsing and blank checks. */
final class Rules {

    private Rules() {
    }

    static Set<String> csv(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> s.toUpperCase().replace(' ', '_'))
            .collect(Collectors.toSet());
    }

    static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
