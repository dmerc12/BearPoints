package com.bearpoints.api.converter;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.utility.GradeLevelUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class StringToGradeLevelConverter implements Converter<String, GradeLevel> {
    @Override
    public GradeLevel convert(@Nullable String source) {
        return GradeLevelUtils.validateAndConvertGrade(source);
    }
}
