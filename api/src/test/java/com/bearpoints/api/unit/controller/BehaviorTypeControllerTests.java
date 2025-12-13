package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.BehaviorTypeController;
import com.bearpoints.api.criteria.BehaviorTypeSearchCriteria;
import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.BehaviorTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BehaviorTypeController}.
 * <p>Verifies functionality of behavior type management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see BehaviorTypeController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorTypeController Unit Tests")
public class BehaviorTypeControllerTests {
    @Mock
    private BehaviorTypeService behaviorTypeService;

    @InjectMocks
    private BehaviorTypeController behaviorTypeController;

    private BehaviorTypeDTO createBehaviorTypeDTO(Long id, String name, Integer pointValue, Boolean active) {
        return new BehaviorTypeDTO(id, name, pointValue, active);
    }

    @Nested
    @DisplayName("When retrieving all behavior types")
    class WhenRetrievingAllBehaviorTypes {
        @Test
        @DisplayName("Should return paginated behavior types with default parameters")
        void shouldReturnPaginatedBehaviorTypesWithDefaultParameters() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true),
                    createBehaviorTypeDTO(2L, "other", 3, false)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(0, 20, "name,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 15L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(1, 10, "name,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with DESC in uppercase")
        void shouldHandleSortingParameterWithDESCInUppercase() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(1, 10, "name,DESC");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with mixed case direction")
        void shouldHandleSortParameterWithMixedCaseDirection() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 15L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(1, 10, "name,DeSc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with single field (no direction)")
        void shouldHandleSortParameterWithSingleFieldNoDirection() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(1, 10, "name");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with invalid direction")
        void shouldHandleSortParameterWithInvalidDirection() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(1, 10, "name,invalid");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching behavior types")
    class WhenSearchingBehaviorTypes {
        @Test
        @DisplayName("Should search behavior types with name criteria")
        void shouldSearchBehaviorTypesWithNameCriteria() {
            String name = "test";
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .searchBehaviorTypes(name, null, null, null, 1, 10, "name,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search behavior types with active criteria")
        void shouldSearchBehaviorTypesWithActiveCriteria() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .searchBehaviorTypes(null, true, null, null, 1, 10, "active,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search behavior types with point value range criteria")
        void shouldSearchBehaviorTypesWithPointValueRangeCriteria() {
            String name = "test";
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .searchBehaviorTypes(name, null, 1, 2, 1, 10, "pointValue,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search behavior types with combined criteria")
        void shouldSearchBehaviorTypesWithCombinedCriteria() {
            String name = "test";
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .searchBehaviorTypes(name, true, 1, 2, 1, 10, "name,asc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle single sort parameter")
        void shouldHandleSingleSortParameter() {
            String name = "test";
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .searchBehaviorTypes(name, null, null, null, 1, 10, "name");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(behaviorTypeService).searchBehaviorTypes(any(BehaviorTypeSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving behavior type by ID")
    class WhenRetrievingBehaviorTypeById {
        @Test
        @DisplayName("Should return behavior type when found")
        void shouldReturnBehaviorTypeWhenFound() {
            Long behaviorTypeId = 1L;
            BehaviorTypeDTO behaviorTypeDTO = createBehaviorTypeDTO(behaviorTypeId, "test", 2, true);
            when(behaviorTypeService.getBehaviorTypeById(behaviorTypeId)).thenReturn(behaviorTypeDTO);
            ResponseEntity<BehaviorTypeDTO> response = behaviorTypeController.getBehaviorTypeById(behaviorTypeId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(behaviorTypeId, response.getBody().getId());
            verify(behaviorTypeService).getBehaviorTypeById(behaviorTypeId);
        }

        @Test
        @DisplayName("Should return 404 when behavior type not found")
        void shouldReturn404WhenBehaviorTypeNotFound() {
            Long behaviorTypeId = 999L;
            when(behaviorTypeService.getBehaviorTypeById(behaviorTypeId))
                    .thenThrow(new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId));
            assertThrows(ResourceNotFoundException.class,
                    () -> behaviorTypeController.getBehaviorTypeById(behaviorTypeId));
            verify(behaviorTypeService).getBehaviorTypeById(behaviorTypeId);
        }
    }

    @Nested
    @DisplayName("When creating behavior type")
    class WhenCreatingBehaviorType {
        @Test
        @DisplayName("Should create new behavior type and return 201 status")
        void shouldCreateNewBehaviorTypeAndReturn201Status() {
            Long behaviorTypeId = 1L;
            String name = "test";
            Integer pointValue = 1;
            Boolean active = true;
            BehaviorTypeDTO behaviorTypeDTO = createBehaviorTypeDTO(null, name, pointValue, active);
            BehaviorTypeDTO createdBehaviorType = createBehaviorTypeDTO(behaviorTypeId, name, pointValue, active);
            when(behaviorTypeService.createBehaviorType(behaviorTypeDTO)).thenReturn(createdBehaviorType);
            ResponseEntity<BehaviorTypeDTO> response = behaviorTypeController.createBehaviorType(behaviorTypeDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(behaviorTypeId, response.getBody().getId());
            assertEquals(name, response.getBody().getName());
            assertEquals(pointValue, response.getBody().getPointValue());
            assertEquals(active, response.getBody().getActive());
            verify(behaviorTypeService).createBehaviorType(behaviorTypeDTO);
        }
    }

    @Nested
    @DisplayName("When updating behavior type")
    class WhenUpdatingBehaviorType {
        @Test
        @DisplayName("Should update existing behavior type and return 200 status")
        void shouldUpdateExistingBehaviorTypeAndReturn200Status() {
            Long behaviorTypeId = 1L;
            String name = "test";
            Integer pointValue = 1;
            Boolean active = true;
            BehaviorTypeDTO behaviorTypeDTO = createBehaviorTypeDTO(null, name, pointValue, active);
            BehaviorTypeDTO updatedBehaviorType = createBehaviorTypeDTO(behaviorTypeId, name, pointValue, active);
            when(behaviorTypeService.updateBehaviorType(behaviorTypeId, behaviorTypeDTO)).thenReturn(updatedBehaviorType);
            ResponseEntity<BehaviorTypeDTO> response = behaviorTypeController.updateBehaviorType(behaviorTypeId, behaviorTypeDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(behaviorTypeId, response.getBody().getId());
            assertEquals(name, response.getBody().getName());
            assertEquals(pointValue, response.getBody().getPointValue());
            assertEquals(active, response.getBody().getActive());
            verify(behaviorTypeService).updateBehaviorType(behaviorTypeId, behaviorTypeDTO);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent behavior type")
        void shouldReturn404WhenUpdatingNonExistentBehaviorType() {
            Long behaviorTypeId = 1L;
            BehaviorTypeDTO behaviorTypeDTO = createBehaviorTypeDTO(null, "nonexistent", 1, false);
            when(behaviorTypeService.updateBehaviorType(behaviorTypeId, behaviorTypeDTO))
                    .thenThrow(new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId));
            assertThrows(ResourceNotFoundException.class,
                    () -> behaviorTypeController.updateBehaviorType(behaviorTypeId, behaviorTypeDTO));
            verify(behaviorTypeService).updateBehaviorType(behaviorTypeId, behaviorTypeDTO);
        }
    }

    @Nested
    @DisplayName("When deleting behavior type")
    class WhenDeletingBehaviorType {
        @Test
        @DisplayName("Should delete behavior type and return 204 status")
        void shouldDeleteBehaviorTypeAndReturn204Status() {
            Long behaviorTypeId = 1L;
            ResponseEntity<Void> response = behaviorTypeController.deleteBehaviorType(behaviorTypeId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(behaviorTypeService).deleteBehaviorType(behaviorTypeId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent behavior type")
        void shouldReturn404WhenDeletingNonExistentBehaviorType() {
            Long behaviorTypeId = 999L;
            doThrow(new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId))
                    .when(behaviorTypeService).deleteBehaviorType(behaviorTypeId);
            assertThrows(ResourceNotFoundException.class,
                    () -> behaviorTypeController.deleteBehaviorType(behaviorTypeId));
            verify(behaviorTypeService).deleteBehaviorType(behaviorTypeId);
        }
    }

    @Nested
    @DisplayName("When testing sort parameter splitting")
    class WhenTestingSortParameterSplitting {
        @Test
        @DisplayName("Should handle sort parameter with multiple commas")
        void shouldHandleSortParameterWithMultipleCommas() {
            List<BehaviorTypeDTO> behaviorTypes = List.of(
                    createBehaviorTypeDTO(1L, "test", 2, true)
            );
            Page<BehaviorTypeDTO> behaviorTypePage = new PageImpl<>(behaviorTypes, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<BehaviorTypeDTO> expectedResponse = PagedResponseDTO.of(behaviorTypePage);
            when(behaviorTypeService.getAllBehaviorTypes(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> response = behaviorTypeController
                    .getAllBehaviorTypes(0, 20, "name,asc,active,asc,pointValue,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(behaviorTypeService).getAllBehaviorTypes(any(Pageable.class));
        }
    }
}
