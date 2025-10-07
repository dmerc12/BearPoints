package com.bearpoints.api.converter;

import com.bearpoints.api.entity.GradeLevel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class StringToGradeLevelConverter implements Converter<String, GradeLevel> {
    @Override
    public GradeLevel convert(@Nullable String source) {
        if (source == null) {
            return null;
        }
        String trimmedSource = source.trim();
        if (trimmedSource.isEmpty()) {
            return null;
        }
        String normalizedSource = trimmedSource.toUpperCase().replace("-", "_");
        try {
            return GradeLevel.valueOf(normalizedSource.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid grade level: " + source + ". Valid values are: " +
                    java.util.Arrays.toString(GradeLevel.values()));
        }
    }
}
