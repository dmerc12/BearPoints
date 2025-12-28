package com.bearpoints.api.unit.config;

import com.bearpoints.api.config.WebMvcConfig;
import com.bearpoints.api.resolver.PaginationAndSortingArgumentResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebMvcConfig}.
 * <p>Verifies configuration and registration of custom argument resolvers in Spring MVC,
 * specifically of pagination and sorting parameters in controller methods.
 *
 * <p>Tests validate proper integration with Spring's Web MVC framework and ensure
 * that custom resolvers are correctly registered without affecting existing functionality.
 *
 * @see WebMvcConfig
 * @see PaginationAndSortingArgumentResolver
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("WebMvcConfig Unit Tests")
public class WebMvcConfigTests {
    @Test
    @DisplayName("Should add PaginationAndSortingArgumentResolver to resolver list")
    void shouldAddPaginationAndSortingArgumentResolverToResolverList() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        webMvcConfig.addArgumentResolvers(resolvers);
        assertEquals(1, resolvers.size());
        assertThat(resolvers.getFirst()).isInstanceOf(PaginationAndSortingArgumentResolver.class);
    }

    @Test
    @DisplayName("Should preserve existing resolvers when adding custom resolver")
    void shouldPreserveExistingResolversWhenAddingCustomResolver() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        HandlerMethodArgumentResolver existingResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NotNull MethodParameter parameter) {
                return false;
            }

            @Override
            public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return null;
            }
        };
        resolvers.add(existingResolver);
        webMvcConfig.addArgumentResolvers(resolvers);
        assertEquals(2, resolvers.size());
        assertSame(existingResolver, resolvers.getFirst());
        assertThat(resolvers.get(1)).isInstanceOf(PaginationAndSortingArgumentResolver.class);
    }

    @Test
    @DisplayName("Should create new PaginationAndSortingArgumentResolver instance")
    void shouldCreateNewPaginationAndSortingArgumentResolverInstance() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        webMvcConfig.addArgumentResolvers(resolvers);
        PaginationAndSortingArgumentResolver resolver = (PaginationAndSortingArgumentResolver) resolvers.getFirst();
        assertNotNull(resolver);
    }

    @Test
    @DisplayName("Should add resolver to empty list")
    void shouldAddResolverToEmptyList() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        webMvcConfig.addArgumentResolvers(resolvers);
        assertFalse(resolvers.isEmpty());
        assertEquals(1, resolvers.size());

    }
}
