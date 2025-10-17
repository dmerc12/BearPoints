package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.PagedResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PagedResponseDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping from Spring Data Page to PagedResponseDTO</li>
 *     <li>All pagination metadata is properly populated</li>
 *     <li>Edge cases including empty pages and boundary conditions</li>
 * </ul>
 *
 * @see PagedResponseDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("PagedResponse DTO Tests")
public class PagedResponseDTOTests {
    private final List<String> sampleContent = Arrays.asList("item1", "item2", "item3");
    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When created from Spring Data Page")
    class WhenCreatedFromSpringDataPage {
        @Test
        @DisplayName("Should correctly map all pagination metadata")
        void shouldCorrectlyMapAllPaginationMetadata() {
            Page<String> page = new PageImpl<>(sampleContent, pageable, 25L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertEquals(sampleContent, response.getContent());
            assertEquals(25L, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
            assertEquals(10, response.getSize());
            assertEquals(0, response.getNumber());
            assertTrue(response.isFirst());
            assertFalse(response.isLast());
            assertEquals(3, response.getNumberOfElements());
            assertFalse(response.isEmpty());

        }

        @Test
        @DisplayName("Should handle empty page correctly")
        void shouldHandleEmptyPageCorrectly() {
            Page<String> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertTrue(response.getContent().isEmpty());
            assertEquals(0L, response.getTotalElements());
            assertEquals(0, response.getTotalPages());
            assertEquals(0, response.getNumberOfElements());
            assertTrue(response.isEmpty());
            assertTrue(response.isFirst());
            assertTrue(response.isLast());
        }

        @Test
        @DisplayName("Should correctly identify last page")
        void shouldCorrectlyIdentifyLastPage() {
            Pageable lastPageable = PageRequest.of(2, 10);
            Page<String> page = new PageImpl<>(sampleContent, lastPageable, 25L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertEquals(2, response.getNumber());
            assertFalse(response.isFirst());
            assertTrue(response.isLast());
            assertEquals(3, response.getTotalPages());
        }

        @Test
        @DisplayName("Should handle single element page correctly")
        void shouldHandleSingleElementPageCorrectly() {
            List<String> singleItem = Collections.singletonList("singleItem");
            Page<String> page = new PageImpl<>(singleItem, pageable, 1L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertEquals(1, response.getContent().size());
            assertEquals(1L, response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(1, response.getNumberOfElements());
            assertTrue(response.isFirst());
            assertTrue(response.isLast());
            assertFalse(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("When using static factory method")
    class WhenUsingStaticFactoryMethod {
        @Test
        @DisplayName("Should create equivalent instance using of() method")
        void shouldCreateEquivalentInstanceUsingOfMethod() {
            Page<String> page = new PageImpl<>(sampleContent, pageable, 15L);
            PagedResponseDTO<String> constructorResponse = new PagedResponseDTO<>(page);
            PagedResponseDTO<String> factoryResponse = PagedResponseDTO.of(page);
            assertEquals(constructorResponse.getContent(), factoryResponse.getContent());
            assertEquals(constructorResponse.getTotalElements(), factoryResponse.getTotalElements());
            assertEquals(constructorResponse.getTotalPages(), factoryResponse.getTotalPages());
            assertEquals(constructorResponse.getSize(), factoryResponse.getSize());
            assertEquals(constructorResponse.getNumber(), factoryResponse.getNumber());
            assertEquals(constructorResponse.isFirst(), factoryResponse.isFirst());
            assertEquals(constructorResponse.isLast(), factoryResponse.isLast());
            assertEquals(constructorResponse.getNumberOfElements(), factoryResponse.getNumberOfElements());
            assertEquals(constructorResponse.isEmpty(), factoryResponse.isEmpty());
        }

        @Test
        @DisplayName("Should handle null page gracefully")
        void shouldHandleNullPageGracefully() {
            assertThrows(NullPointerException.class, () ->
                    PagedResponseDTO.of(null));
        }
    }

    @Nested
    @DisplayName("When validating pagination calculations")
    class WhenValidatingPaginationCalculations {
        @Test
        @DisplayName("Should calculate total pages correctly for exact multiples")
        void shouldCalculateTotalPagesCorrectlyForExactMultiples() {
            Page<String> page = new PageImpl<>(sampleContent, PageRequest.of(0, 5), 15L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertEquals(3, response.getTotalPages());
            assertEquals(15L, response.getTotalElements());
            assertEquals(5, response.getSize());
        }

        @Test
        @DisplayName("Should calculate total pages correctly for partial pages")
        void shouldCalculateTotalPagesCorrectlyForPartialPages() {
            Page<String> page = new PageImpl<>(sampleContent, PageRequest.of(0, 2), 7L);
            PagedResponseDTO<String> response = new PagedResponseDTO<>(page);
            assertEquals(4, response.getTotalPages());
            assertEquals(7L, response.getTotalElements());
            assertEquals(2, response.getSize());
        }
    }
}
