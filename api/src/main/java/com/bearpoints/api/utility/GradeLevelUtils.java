package com.bearpoints.api.utility;

import com.bearpoints.api.entity.GradeLevel;

public class GradeLevelUtils {
    private GradeLevelUtils() {
        // Utility class - prevent instantiation
    }

    public static GradeLevel validateAndConvertGrade(String gradeString) {
        if (gradeString == null) {
            return null;
        }
        String trimmed = gradeString.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toUpperCase().replace("-", "_");
        try {
            return GradeLevel.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid grade level: " + gradeString +
                    ". Valid values are: " + java.util.Arrays.toString(GradeLevel.values())));
        }
    }
}
