package com.bearpoints.api.unit.service;

import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.RewardItemDTO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.RewardItemServiceImpl;
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
 * Unit tests for {@link RewardItemServiceImpl}.
 * <p>Verifies reward item management functionality including CRUD operations and
 * search with criteria.
 *
 * @see RewardItemServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RewardItemService Tests")
public class RewardItemServiceTests {
    @Mock
    private RewardItemDAO rewardItemDAO;

    @InjectMocks
    private RewardItemServiceImpl rewardItemService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When retrieving all reward items")
    class WhenRetrievingAllRewardItems {
        @Test
        @DisplayName("Should retrieve all reward items with pagination")
        void shouldRetrieveAllRewardItemsWithPagination() {
            List<RewardItem> rewardItems = List.of(
                    createRewardItem(1L, "Pencil", 3, 50),
                    createRewardItem(2L, "Sticker", 5, 100)
            );
            Page<RewardItem> rewardItemPage = new PageImpl<>(rewardItems, pageable, 2L);
            when(rewardItemDAO.findAll(any(Pageable.class))).thenReturn(rewardItemPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.getAllRewardItems(pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(2L, result.getTotalElements());
            verify(rewardItemDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no reward items exist")
        void shouldReturnEmptyPageWhenNoRewardItemsExist() {
            Page<RewardItem> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(rewardItemDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.getAllRewardItems(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching reward items with criteria")
    class WhenSearchingRewardItemsWithCriteria {
        @Test
        @DisplayName("Should search reward items with name criteria")
        void shouldSearchRewardItemsWithNameCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setName("Test");
            List<RewardItem> rewardItems = List.of(createRewardItem(1L, "Test", 3, 50));
            Page<RewardItem> rewardItemPage = new PageImpl<>(rewardItems, pageable, 2L);
            when(rewardItemDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(rewardItemPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.searchRewardItems(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(rewardItemDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search reward items with point cost range criteria")
        void shouldSearchRewardItemsWithPointCostRangeCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinPointCost(2);
            criteria.setMaxPointCost(5);
            List<RewardItem> rewardItems = List.of(createRewardItem(1L, "Test", 3, 50));
            Page<RewardItem> rewardItemPage = new PageImpl<>(rewardItems, pageable, 2L);
            when(rewardItemDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(rewardItemPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.searchRewardItems(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(rewardItemDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search reward items with stock range criteria")
        void shouldSearchRewardItemsWithStockRangeCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinStock(20);
            criteria.setMaxStock(60);
            List<RewardItem> rewardItems = List.of(createRewardItem(1L, "Test", 3, 50));
            Page<RewardItem> rewardItemPage = new PageImpl<>(rewardItems, pageable, 2L);
            when(rewardItemDAO.findAll(any(Specification.class), any(Pageable.class))).thenReturn(rewardItemPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.searchRewardItems(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(rewardItemDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all reward items with no criteria specified")
        void shouldReturnAllRewardItemsWithNoCriteriaSpecified() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            List<RewardItem> rewardItems = List.of(
                    createRewardItem(1L, "Test", 3, 50),
                    createRewardItem(2L, "Sticker", 10, 80)
            );
            Page<RewardItem> rewardItemPage = new PageImpl<>(rewardItems, pageable, 2L);
            when(rewardItemDAO.findAll(any(Pageable.class))).thenReturn(rewardItemPage);
            PagedResponseDTO<RewardItemDTO> result = rewardItemService.searchRewardItems(criteria, pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(rewardItemDAO).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("When retrieving reward item by identifier")
    class WhenRetrievingRewardItemById {
        @Test
        @DisplayName("Should return reward item by ID when found")
        void shouldReturnRewardItemByIdWhenFound() {
            Long rewardItemId = 1L;
            RewardItem rewardItem = createRewardItem(rewardItemId, "Test", 1, 2);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(rewardItem));
            RewardItemDTO result = rewardItemService.getRewardItemById(rewardItemId);
            assertNotNull(result);
            assertEquals(rewardItemId, result.getId());
            assertEquals(1, result.getPointCost());
            assertEquals(2, result.getStock());
            verify(rewardItemDAO).findById(rewardItemId);
        }

        @Test
        @DisplayName("Should return ResourceNotFoundException when reward item not found by ID")
        void shouldReturnResourceNotFoundExceptionWhenRewardItemNotFoundById() {
            Long rewardItemId = 999L;
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemService.getRewardItemById(rewardItemId));
            verify(rewardItemDAO).findById(rewardItemId);
        }
    }

    @Nested
    @DisplayName("When creating reward item")
    class WhenCreatingRewardItem {
        @Test
        @DisplayName("Should create new reward item successfully")
        void shouldCreateNewRewardItemSuccessfully() {
            String name = "Test";
            Integer pointCost = 3;
            Integer stock = 10;
            RewardItemDTO rewardItemDTO = new RewardItemDTO(null, name, pointCost, stock);
            RewardItem savedRewardItem = createRewardItem(1L, name, pointCost, stock);
            when(rewardItemDAO.findByName(name)).thenReturn(Optional.empty());
            when(rewardItemDAO.save(any(RewardItem.class))).thenReturn(savedRewardItem);
            RewardItemDTO result = rewardItemService.createRewardItem(rewardItemDTO);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(name, result.getName());
            assertEquals(pointCost, result.getPointCost());
            assertEquals(stock, result.getStock());
            verify(rewardItemDAO).findByName(name);
            verify(rewardItemDAO).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when name already exists")
        void shouldThrowDuplicateResourceExceptionWhenNameAlreadyExists() {
            String name = "Test";
            Integer pointCost = 3;
            Integer stock = 10;
            RewardItemDTO rewardItemDTO = new RewardItemDTO(null, name, pointCost, stock);
            RewardItem existingRewardItem = createRewardItem(1L, name, pointCost, stock);
            when(rewardItemDAO.findByName(name)).thenReturn(Optional.of(existingRewardItem));
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> rewardItemService.createRewardItem(rewardItemDTO)
            );
            assertEquals("A reward item with this name already exists", exception.getMessage());
            verify(rewardItemDAO).findByName(name);
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }
    }

    @Nested
    @DisplayName("When updating reward item")
    class WhenUpdatingRewardItem {
        @Test
        @DisplayName("Should update existing reward item successfully")
        void shouldUpdateExistingRewardItemSuccessfully() {
            Long rewardItemId = 1L;
            String newName = "Updated";
            Integer newPointCost = 6;
            Integer newStock = 15;
            RewardItemDTO updateDTO = new RewardItemDTO(rewardItemId, newName, newPointCost, newStock);
            RewardItem updatedRewardItem = createRewardItem(rewardItemId, newName, newPointCost, newStock);
            RewardItem existingRewardItem = createRewardItem(rewardItemId, "Test", 1, 5);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(existingRewardItem));
            when(rewardItemDAO.findByName(newName)).thenReturn(Optional.empty());
            when(rewardItemDAO.save(any(RewardItem.class))).thenReturn(updatedRewardItem);
            RewardItemDTO result = rewardItemService.updateRewardItem(rewardItemId, updateDTO);
            assertNotNull(result);
            assertEquals(rewardItemId, result.getId());
            assertEquals(newName, result.getName());
            assertEquals(newPointCost, result.getPointCost());
            assertEquals(newStock, result.getStock());
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO).findByName(newName);
            verify(rewardItemDAO).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("Should update reward item without checking name when name unchanged")
        void shouldUpdateRewardItemWithoutCheckingNameWhenNameUnchanged() {
            Long rewardItemId = 1L;
            String sameName = "Same";
            Integer newPointCost = 6;
            Integer newStock = 15;
            RewardItemDTO updateDTO = new RewardItemDTO(rewardItemId, sameName, newPointCost, newStock);
            RewardItem updatedRewardItem = createRewardItem(rewardItemId, sameName, newPointCost, newStock);
            RewardItem existingRewardItem = createRewardItem(rewardItemId, sameName, 1, 5);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(existingRewardItem));
            when(rewardItemDAO.save(any(RewardItem.class))).thenReturn(updatedRewardItem);
            RewardItemDTO result = rewardItemService.updateRewardItem(rewardItemId, updateDTO);
            assertNotNull(result);
            assertEquals(rewardItemId, result.getId());
            assertEquals(sameName, result.getName());
            assertEquals(newPointCost, result.getPointCost());
            assertEquals(newStock, result.getStock());
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO, never()).findByName(anyString());
            verify(rewardItemDAO).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing name")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingName() {
            Long rewardItemId = 1L;
            Long otherRewardItemId = 2L;
            String oldName = "Old";
            String existingName = "Existing";
            Integer newPointCost = 6;
            Integer newStock = 15;
            RewardItemDTO updateDTO = new RewardItemDTO(rewardItemId, existingName, newPointCost, newStock);
            RewardItem otherRewardItem = createRewardItem(otherRewardItemId, existingName, 5, 8);
            RewardItem existingRewardItem = createRewardItem(rewardItemId, oldName, 1, 5);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(existingRewardItem));
            when(rewardItemDAO.findByName(existingName)).thenReturn(Optional.of(otherRewardItem));
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> rewardItemService.updateRewardItem(rewardItemId, updateDTO)
            );
            assertEquals("A reward item with this name already exists", exception.getMessage());
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO).findByName(existingName);
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("Should return ResourceNotFoundException when reward item not found")
        void shouldReturnResourceNotFoundExceptionWhenRewardItemNotFound() {
            Long rewardItemId = 9999L;
            String newName = "Updated";
            Integer newPointCost = 6;
            Integer newStock = 15;
            RewardItemDTO updateDTO = new RewardItemDTO(rewardItemId, newName, newPointCost, newStock);
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemService.updateRewardItem(rewardItemId, updateDTO)
            );
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO, never()).findByName(anyString());
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("Should allow update when name exists but is same reward item")
        void shouldAllowUpdateWhenNameExistsAndIsSameRewardItem() {
            Long rewardItemId = 1L;
            String newName = "Updated";
            Integer newPointCost = 6;
            Integer newStock = 15;
            RewardItemDTO updateDTO = new RewardItemDTO(rewardItemId, newName, newPointCost, newStock);
            RewardItem existingRewardItem = createRewardItem(rewardItemId, "Test", 1, 5);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(existingRewardItem));
            when(rewardItemDAO.findByName(newName)).thenReturn(Optional.of(existingRewardItem));
            when(rewardItemDAO.save(any(RewardItem.class))).thenReturn(existingRewardItem);
            RewardItemDTO result = rewardItemService.updateRewardItem(rewardItemId, updateDTO);
            assertNotNull(result);
            assertEquals(rewardItemId, result.getId());
            assertEquals(newName, result.getName());
            assertEquals(newPointCost, result.getPointCost());
            assertEquals(newStock, result.getStock());
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO).findByName(newName);
            verify(rewardItemDAO).save(existingRewardItem);
        }
    }

    @Nested
    @DisplayName("When deleting reward item")
    class WhenDeletingRewardItem {
        @Test
        @DisplayName("Should delete reward item successfully")
        void shouldDeleteRewardItemSuccessfully() {
            Long rewardItemId = 1L;
            RewardItem rewardItem = createRewardItem(rewardItemId, "Test", 1, 5);
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.of(rewardItem));
            rewardItemService.deleteRewardItem(rewardItemId);
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO).delete(rewardItem);
        }

        @Test
        @DisplayName("Should return ResourceNotFoundException when reward item not found")
        void shouldReturnResourceNotFoundExceptionWhenRewardItemNotFound() {
            Long rewardItemId = 9999L;
            when(rewardItemDAO.findById(rewardItemId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemService.deleteRewardItem(rewardItemId)
            );
            verify(rewardItemDAO).findById(rewardItemId);
            verify(rewardItemDAO, never()).delete(any(RewardItem.class));
        }
    }

    private RewardItem createRewardItem(Long id, String name, Integer pointCost, Integer stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setId(id);
        rewardItem.setName(name);
        rewardItem.setPointCost(pointCost);
        rewardItem.setStock(stock);
        return rewardItem;
    }
}
