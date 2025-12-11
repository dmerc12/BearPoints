package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.BehaviorTypeSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.BehaviorTypeServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BehaviorTypeServiceImpl}.
 * <p>Verifies behavior type management functionality including CRUD operations and
 * search with criteria.
 *
 * @see BehaviorTypeServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BehaviorTypeService Tests")
public class BehaviorTypeServiceTests {
    @Mock
    private BehaviorTypeDAO behaviorTypeDAO;

    @InjectMocks
    private BehaviorTypeServiceImpl behaviorTypeService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When retrieving all behavior types")
    class WhenRetrievingAllBehaviorTypes {
        @Test
        @DisplayName("Should retrieve all behavior types with pagination")
        void shouldRetrieveAllBehaviorTypesWithPagination() {
            List<BehaviorType> behaviorTypes = List.of(
                    createBehaviorType(1L, "Test Behavior 1", 1, true),
                    createBehaviorType(2L, "Test Behavior 2", 2, false)
            );
            Page<BehaviorType> behaviorTypePage = new PageImpl<>(behaviorTypes, pageable, 2L);
            when(behaviorTypeDAO.findAll(any(Pageable.class))).thenReturn(behaviorTypePage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.getAllBehaviorTypes(pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(2L, result.getTotalElements());
            verify(behaviorTypeDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no behavior types exist")
        void shouldReturnEmptyPageWhenNoBehaviorTypesExist() {
            Page<BehaviorType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(behaviorTypeDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.getAllBehaviorTypes(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching behavior types with criteria")
    class WhenSearchingBehaviorTypesWithCriteria {
        @Test
        @DisplayName("Should search behavior types with name criteria")
        void shouldSearchBeBehaviorTypesWithNameCriteria() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setName("Test");
            List<BehaviorType> behaviorTypes = List.of(createBehaviorType(1L, "Test", 1, true));
            Page<BehaviorType> behaviorTypePage = new PageImpl<>(behaviorTypes, pageable, 1L);
            when(behaviorTypeDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(behaviorTypePage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.searchBehaviorTypes(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(behaviorTypeDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search behavior types with active criteria")
        void shouldSearchBeBehaviorTypesWithActiveCriteria() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setActive(true);
            List<BehaviorType> behaviorTypes = List.of(createBehaviorType(1L, "Test", 1, true));
            Page<BehaviorType> behaviorTypePage = new PageImpl<>(behaviorTypes, pageable, 1L);
            when(behaviorTypeDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(behaviorTypePage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.searchBehaviorTypes(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(behaviorTypeDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search behavior types with points range criteria")
        void shouldSearchBeBehaviorTypesWithPointsRangeCriteria() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setMinPointValue(1);
            criteria.setMaxPointValue(2);
            List<BehaviorType> behaviorTypes = List.of(createBehaviorType(1L, "Test", 1, true));
            Page<BehaviorType> behaviorTypePage = new PageImpl<>(behaviorTypes, pageable, 1L);
            when(behaviorTypeDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(behaviorTypePage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.searchBehaviorTypes(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(behaviorTypeDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all behavior types with no criteria specified")
        void shouldReturnAllBehaviorTypesWhenNoCriteriaSpecified() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            List<BehaviorType> behaviorTypes = List.of(
                    createBehaviorType(1L, "Test 1", 1, true),
                    createBehaviorType(2L, "Test 2", 2, false)
            );
            Page<BehaviorType> behaviorTypePage = new PageImpl<>(behaviorTypes, pageable, 1L);
            when(behaviorTypeDAO.findAll(any(Pageable.class))).thenReturn(behaviorTypePage);
            PagedResponseDTO<BehaviorTypeDTO> result = behaviorTypeService.searchBehaviorTypes(criteria, pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(behaviorTypeDAO).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("When retrieving behavior type by identifier")
    class WhenRetrievingStudentByIdentifier {
        @Test
        @DisplayName("Should return behavior type by ID when found")
        void shouldReturnBeBehaviorTypeByIdWhenFound() {
            Long behaviorTypeId = 1L;
            BehaviorType behaviorType = createBehaviorType(behaviorTypeId, "Test", 1, true);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(behaviorType));
            BehaviorTypeDTO result = behaviorTypeService.getBehaviorTypeById(behaviorTypeId);
            assertNotNull(result);
            assertEquals(behaviorTypeId, result.getId());
            assertEquals(1, result.getPointValue());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when behavior type not found by ID")
        void shouldThrowResourceNotFoundExceptionWhenBehaviorTypeNotFoundById() {
            Long behaviorTypeId = 999L;
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> behaviorTypeService.getBehaviorTypeById(behaviorTypeId));
            verify(behaviorTypeDAO).findById(behaviorTypeId);
        }
    }

    @Nested
    @DisplayName("When creating behavior type")
    class WhenCreatingBehaviorType {
        @Test
        @DisplayName("Should create new behavior type successfully")
        void shouldCreateNewBeBehaviorTypeSuccessfully() {
            String name = "Test";
            Boolean active = true;
            Integer pointValue = 1;
            BehaviorTypeDTO behaviorTypeDTO = new BehaviorTypeDTO(null, name, pointValue, active);
            BehaviorType savedBehaviorType = createBehaviorType(1L, name, pointValue, active);
            when(behaviorTypeDAO.findByName(name)).thenReturn(Optional.empty());
            when(behaviorTypeDAO.save(any(BehaviorType.class))).thenReturn(savedBehaviorType);
            BehaviorTypeDTO result = behaviorTypeService.createBehaviorType(behaviorTypeDTO);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(name, result.getName());
            assertEquals(pointValue, result.getPointValue());
            assertEquals(active, result.getActive());
            verify(behaviorTypeDAO).save(any(BehaviorType.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when name already exists")
        void shouldThrowDuplicateResourceExceptionWhenNameAlreadyExists() {
            String name = "Test";
            Boolean active = true;
            Integer pointValue = 1;
            BehaviorTypeDTO behaviorTypeDTO = new BehaviorTypeDTO(null, name, pointValue, active);
            BehaviorType existingBehaviorType = createBehaviorType(1L, name, pointValue, active);
            when(behaviorTypeDAO.findByName(name)).thenReturn(Optional.of(existingBehaviorType));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> behaviorTypeService.createBehaviorType(behaviorTypeDTO)
            );
            assertEquals("A behavior type with this name already exists", exception.getMessage());
            verify(behaviorTypeDAO).findByName(name);
            verify(behaviorTypeDAO, never()).save(any(BehaviorType.class));
        }
    }

    @Nested
    @DisplayName("When updating behavior type")
    class WhenUpdatingBehaviorType {
        @Test
        @DisplayName("Should update existing behavior type successfully")
        void shouldUpdateExistingBeBehaviorTypeSuccessfully() {
            Long behaviorTypeId = 1L;
            String newName = "Updated";
            Integer newPointValue = 3;
            Boolean newActive = false;
            BehaviorType existingBehaviorType = createBehaviorType(behaviorTypeId, "Test", 1, true);
            BehaviorTypeDTO updateDTO = new BehaviorTypeDTO(behaviorTypeId, newName, newPointValue, newActive);
            BehaviorType updatedBehaviorType = createBehaviorType(behaviorTypeId, newName, newPointValue, newActive);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(existingBehaviorType));
            when(behaviorTypeDAO.findByName(newName)).thenReturn(Optional.empty());
            when(behaviorTypeDAO.save(any(BehaviorType.class))).thenReturn(updatedBehaviorType);
            BehaviorTypeDTO result = behaviorTypeService.updateBehaviorType(behaviorTypeId, updateDTO);
            assertNotNull(result);
            assertEquals(behaviorTypeId, result.getId());
            assertEquals(newName, result.getName());
            assertEquals(newPointValue, result.getPointValue());
            assertEquals(newActive, result.getActive());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(behaviorTypeDAO).findByName(newName);
            verify(behaviorTypeDAO).save(any(BehaviorType.class));
        }

        @Test
        @DisplayName("Should update behavior type without checking name when name unchanged")
        void shouldUpdateBehaviorTypeWithoutCheckingNameWhenNameUnchanged() {
            Long behaviorTypeId = 1L;
            String sameName = "Same";
            Integer newPointValue = 3;
            Boolean newActive = false;
            BehaviorType existingBehaviorType = createBehaviorType(behaviorTypeId, sameName, 1, true);
            BehaviorTypeDTO updateDTO = new BehaviorTypeDTO(behaviorTypeId, sameName, newPointValue, newActive);
            BehaviorType updatedBehaviorType = createBehaviorType(behaviorTypeId, sameName, newPointValue, newActive);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(existingBehaviorType));
            when(behaviorTypeDAO.save(any(BehaviorType.class))).thenReturn(updatedBehaviorType);
            BehaviorTypeDTO result = behaviorTypeService.updateBehaviorType(behaviorTypeId, updateDTO);
            assertNotNull(result);
            assertEquals(behaviorTypeId, result.getId());
            assertEquals(sameName, result.getName());
            assertEquals(newPointValue, result.getPointValue());
            assertEquals(newActive, result.getActive());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(behaviorTypeDAO, never()).findByName(anyString());
            verify(behaviorTypeDAO).save(any(BehaviorType.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing name")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingName() {
            Long behaviorTypeId = 1L;
            Long otherBehaviorTypeId = 2L;
            String oldName = "Old";
            String existingName = "Existing";
            Integer newPointValue = 3;
            Boolean newActive = false;
            BehaviorType existingBehaviorType = createBehaviorType(behaviorTypeId, oldName, 1, true);
            BehaviorType otherBehaviorType = createBehaviorType(otherBehaviorTypeId, existingName, 5, true);
            BehaviorTypeDTO updateDTO = new BehaviorTypeDTO(behaviorTypeId, existingName, newPointValue, newActive);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(existingBehaviorType));
            when(behaviorTypeDAO.findByName(existingName)).thenReturn(Optional.of(otherBehaviorType));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> behaviorTypeService.updateBehaviorType(behaviorTypeId, updateDTO)
            );
            assertEquals("A behavior type with this name already exists", exception.getMessage());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(behaviorTypeDAO).findByName(existingName);
            verify(behaviorTypeDAO, never()).save(any(BehaviorType.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when behavior type not found")
        void shouldThrowResourceNotFoundExceptionWhenBehaviorTypeNotFound() {
            Long behaviorTypeId = 999L;
            String newName = "Updated";
            Integer newPointValue = 3;
            Boolean newActive = false;
            BehaviorTypeDTO updateDTO = new BehaviorTypeDTO(behaviorTypeId, newName, newPointValue, newActive);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> behaviorTypeService.updateBehaviorType(behaviorTypeId, updateDTO)
            );
        }

        @Test
        @DisplayName("Should allow update when name exists but is same behavior type")
        void shouldAllowUpdateWhenNameExistsAndIsSameBeBehaviorType() {
            Long behaviorTypeId = 1L;
            String newName = "Updated";
            Integer newPointValue = 3;
            Boolean newActive = false;
            BehaviorType existingBehaviorType = createBehaviorType(behaviorTypeId, "Test", 1, true);
            BehaviorTypeDTO updateDTO = new BehaviorTypeDTO(behaviorTypeId, newName, newPointValue, newActive);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(existingBehaviorType));
            when(behaviorTypeDAO.findByName(newName)).thenReturn(Optional.of(existingBehaviorType));
            when(behaviorTypeDAO.save(any(BehaviorType.class))).thenReturn(existingBehaviorType);
            BehaviorTypeDTO result = behaviorTypeService.updateBehaviorType(behaviorTypeId, updateDTO);
            assertNotNull(result);
            assertEquals(behaviorTypeId, result.getId());
            assertEquals(newName, result.getName());
            assertEquals(newPointValue, result.getPointValue());
            assertEquals(newActive, result.getActive());
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(behaviorTypeDAO).findByName(newName);
            verify(behaviorTypeDAO).save(existingBehaviorType);
        }
    }

    @Nested
    @DisplayName("When deleting behavior type")
    class WhenDeletingBehaviorType {
        @Test
        @DisplayName("Should delete behavior type successfully")
        void shouldDeleteBeBehaviorTypeSuccessfully() {
            Long behaviorTypeId = 1L;
            BehaviorType behaviorType = createBehaviorType(behaviorTypeId, "Test", 1, true);
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.of(behaviorType));
            behaviorTypeService.deleteBehaviorType(behaviorTypeId);
            verify(behaviorTypeDAO).findById(behaviorTypeId);
            verify(behaviorTypeDAO).delete(behaviorType);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when behavior type not found")
        void shouldThrowResourceNotFoundExceptionWhenBehaviorTypeNotFound() {
            Long behaviorTypeId = 999L;
            when(behaviorTypeDAO.findById(behaviorTypeId)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> behaviorTypeService.deleteBehaviorType(behaviorTypeId)
            );
        }
    }

    private BehaviorType createBehaviorType(Long id, String name, Integer pointValue, Boolean active) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(id);
        behaviorType.setName(name);
        behaviorType.setActive(active);
        behaviorType.setPointValue(pointValue);
        return behaviorType;
    }
}
