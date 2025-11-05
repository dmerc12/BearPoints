package com.bearpoints.api.converter;

import com.bearpoints.api.entity.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {

    @Override
    public Role convert(@Nullable String source) {
        if (source == null) {
            return null;
        }
        String trimmedSource = source.trim();
        if (trimmedSource.isEmpty()) {
            return null;
        }
        String normalizedSource = trimmedSource.toUpperCase();
        try {
            return Role.valueOf(normalizedSource);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + source + ". Valid values are: " +
                    java.util.Arrays.toString(Role.values()));
        }
    }
}
