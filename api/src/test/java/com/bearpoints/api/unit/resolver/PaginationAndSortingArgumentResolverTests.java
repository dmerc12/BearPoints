package com.bearpoints.api.unit.resolver;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.resolver.PaginationAndSortingArgumentResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PaginationAndSortingArgumentResolver}.
 * <p>Verifies the behavior of the custom Spring MVC argument resolver that automatically
 * converts HTTP request parameters into {@link Pageable} objects using the
 * {@link PaginationAndSorting} annotation.
 *
 * <p>Tests validate the resolver's ability to:
 * <ul>
 *     <li>Determine which controller method parameters it supports</li>
 *     <li>Extract pagination and sorting parameters from HTTP requests</li>
 *     <li>Apply default values when parameters are missing</li>
 *     <li>Enforce validation rules for sort properties</li>
 *     <li>Handle boundary conditions and invalid input gracefully</li>
 *     <li>Support custom parameter names and configuration options</li>
 * </ul>
 *
 * <p>This resolver enables clean controller method signatures by automatically
 * converting HTTP parameters (page, size, sort) into Spring Data's {@link Pageable}
 * objects with full pagination and sorting support.
 *
 * @see PaginationAndSortingArgumentResolver
 * @see PaginationAndSorting
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaginationAndSortingArgumentResolver Tests")
public class PaginationAndSortingArgumentResolverTests {
    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer modelAndViewContainer;

    @Mock
    private WebDataBinderFactory webDataBinderFactory;

    private PaginationAndSortingArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PaginationAndSortingArgumentResolver();
    }

    @Nested
    @DisplayName("supportsParameter Method")
    class SupportsParameterTests {
        @Test
        @DisplayName("Should return true for Pageable parameter with @PaginationAndSorting annotation")
        void shouldReturnTrueForPageableParameterWithAnnotation() {
            when(methodParameter.getParameterType()).thenAnswer(_ -> Pageable.class);
            when(methodParameter.hasParameterAnnotation(PaginationAndSorting.class)).thenReturn(true);
            boolean supports = resolver.supportsParameter(methodParameter);
            assertTrue(supports);
        }

        @Test
        @DisplayName("Should return false for parameter without @PaginationAndSorting annotation")
        void shouldReturnFalseForParameterWithoutAnnotation() {
            when(methodParameter.hasParameterAnnotation(PaginationAndSorting.class)).thenReturn(false);
            boolean supports = resolver.supportsParameter(methodParameter);
            assertFalse(supports);
        }

        @Test
        @DisplayName("Should return false for non-Pageable parameter with @PaginationAndSorting annotation")
        void shouldReturnFalseForNonPageableParameterWithAnnotation() {
            when(methodParameter.hasParameterAnnotation(PaginationAndSorting.class)).thenReturn(true);
            when(methodParameter.getParameterType()).thenAnswer(_ -> String.class);
            boolean supports = resolver.supportsParameter(methodParameter);
            assertFalse(supports);
        }
    }

    @Nested
    @DisplayName("resolveArgument Method")
    class ResolveArgumentTests {
        @Test
        @DisplayName("Should resolve Pageable with default values when no parameters provided")
        void shouldResolvePageableWithDefaultValuesWhenNoParametersProvided() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn(null);
            when(webRequest.getParameter("size")).thenReturn(null);
            when(webRequest.getParameter("sort")).thenReturn(null);
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(0, pageable.getPageNumber());
            assertEquals(20, pageable.getPageSize());
            assertTrue(pageable.getSort().isSorted());
            assertEquals("id", pageable.getSort().iterator().next().getProperty());
            assertEquals(Sort.Direction.ASC, pageable.getSort().iterator().next().getDirection());
        }

        @Test
        @DisplayName("Should resolve Pageable with custom parameters")
        void shouldResolvePageableWithCustomParameters() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("2");
            when(webRequest.getParameter("size")).thenReturn("50");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(2, pageable.getPageNumber());
            assertEquals(50, pageable.getPageSize());
            assertEquals("name", pageable.getSort().iterator().next().getProperty());
            assertEquals(Sort.Direction.DESC, pageable.getSort().iterator().next().getDirection());
        }

        @Test
        @DisplayName("Should enforce maximum page size")
        void shouldEnforceMaximumPageSize() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("200");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(100, pageable.getPageSize());
        }

        @Test
        @DisplayName("Should enforce minimum page size")
        void shouldEnforceMinimumPageSize() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("0");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(1, pageable.getPageSize());
        }

        @Test
        @DisplayName("Should handle invalid page parameter")
        void shouldHandleInvalidPageParameter() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("invalid");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(0, pageable.getPageNumber());
        }

        @Test
        @DisplayName("Should handle invalid size parameter")
        void shouldHandleInvalidSizeParameter() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("invalid");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(20, pageable.getPageSize());
        }

        @Test
        @DisplayName("Should handle negative page parameter")
        void shouldHandleNegativePageParameter() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("-1");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(0, pageable.getPageNumber());
        }

        @Test
        @DisplayName("Should validate sort string when validation is enabled")
        void shouldValidateSortStringWhenValidationIsEnabled() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithAllowedProperties", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("-1");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("invalidProperty,asc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertFalse(pageable.getSort().isSorted());
        }

        @Test
        @DisplayName("Should filter allowed sort properties")
        void shouldFilterAllowedSortProperties() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithAllowedProperties", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,asc;notAllowed,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertFalse(pageable.getSort().isSorted());
        }

        @Test
        @DisplayName("Should allow all sort properties when none specified")
        void shouldAllowAllSortPropertiesWhenNoneSpecified() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("anyProperty,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertTrue(pageable.getSort().isSorted());
            assertEquals("anyProperty", pageable.getSort().iterator().next().getProperty());
        }

        @Test
        @DisplayName("Should use custom parameter names")
        void shouldUseCustomParameterNames() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithCustomParams", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("p")).thenReturn("3");
            when(webRequest.getParameter("s")).thenReturn("30");
            when(webRequest.getParameter("order")).thenReturn("name,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertEquals(3, pageable.getPageNumber());
            assertEquals(30, pageable.getPageSize());
            assertEquals("name", pageable.getSort().iterator().next().getProperty());
            assertEquals(Sort.Direction.DESC, pageable.getSort().iterator().next().getDirection());
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethod", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,asc;pointValue,desc;active");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            var orders = pageable.getSort().toList();
            assertEquals(3, orders.size());
            assertEquals("name", orders.getFirst().getProperty());
            assertEquals(Sort.Direction.ASC, orders.getFirst().getDirection());
            assertEquals("pointValue", orders.get(1).getProperty());
            assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
            assertEquals("active", orders.get(2).getProperty());
            assertEquals(Sort.Direction.ASC, orders.get(2).getDirection());
        }

        @Test
        @DisplayName("Should return unsorted when sort becomes empty after validation")
        void shouldReturnUnsortedWhenSortBecomesEmptyAfterValidation() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithAllowedProperties", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn(",asc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertFalse(pageable.getSort().isSorted());
        }

        @Test
        @DisplayName("Should return sort string when all properties are allowed")
        void shouldReturnSortStringWhenAllPropertiesAreAllowed() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithAllowedProperties", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,asc;pointValue,desc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertTrue(pageable.getSort().isSorted());
            assertEquals(2, pageable.getSort().toList().size());
        }

        @Test
        @DisplayName("Should skip validation when validateSort is false")
        void shouldSkipValidationWhenValidateSortIsFalse() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithValidationDisabled", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn("name,asc");
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertTrue(pageable.getSort().isSorted());
            assertEquals("name", pageable.getSort().iterator().next().getProperty());
        }

        @Test
        @DisplayName("Should handle empty default sort string")
        void shouldHandleEmptyDefaultSortString() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("testMethodWithEmptyDefaultSort", Pageable.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
            when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(annotation);
            when(webRequest.getParameter("page")).thenReturn("0");
            when(webRequest.getParameter("size")).thenReturn("20");
            when(webRequest.getParameter("sort")).thenReturn(null);
            Pageable pageable = (Pageable) resolver.resolveArgument(
                    methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
            );
            assertNotNull(pageable);
            assertFalse(pageable.getSort().isSorted());
        }
    }

    @Test
    @DisplayName("Should return null when annotation is null")
    void shouldReturnNullWhenAnnotationIsNull() {
        when(methodParameter.getParameterAnnotation(PaginationAndSorting.class)).thenReturn(null);
        Object result = resolver.resolveArgument(
                methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory
        );
        assertNull(result);
    }

    @SuppressWarnings("unused")
    static class TestController {
        public void testMethod(@PaginationAndSorting Pageable pageable) {}

        public void testMethodWithAllowedProperties(
                @PaginationAndSorting(allowedSortProperties = {"name", "pointValue"})
                Pageable pageable
        ) {}

        public void testMethodWithValidationDisabled(
                @PaginationAndSorting(validateSort = false)
                Pageable pageable
        ) {}

        public void testMethodWithEmptyDefaultSort(
                @PaginationAndSorting(defaultSort = "")
                Pageable pageable
        ) {}

        public void testMethodWithCustomParams(
                @PaginationAndSorting(pageParam = "p", sizeParam = "s", sortParam = "order")
                Pageable pageable
        ) {}
    }
}
