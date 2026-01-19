package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.RewardItemController;
import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.RewardItemDTO;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.RewardItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewardItemController}.
 * <p>Verifies functionality of reward item management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see RewardItemController
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RewardItemController Unit Tests")
public class RewardItemControllerTests {
    @Mock
    private RewardItemService rewardItemService;

    @InjectMocks
    private RewardItemController rewardItemController;

    private RewardItemDTO createRewardItemDTO(Long id, String name, Integer pointCost, Integer stock) {
        return new RewardItemDTO(id, name, pointCost, stock);
    }

    @Nested
    @DisplayName("GET /api/items - When retrieving all reward items")
    class WhenRetrievingAllRewardItems {
        @Test
        @DisplayName("Should return paginated reward items with default parameters")
        void shouldReturnPaginatedRewardItemsWithDefaultParameters() {
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35),
                    createRewardItemDTO(2L, "sticker", 6, 90)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")),
                    2L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.getAllRewardItems(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .getAllRewardItems(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(rewardItemService).getAllRewardItems(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "pointCost")),
                    15L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.getAllRewardItems(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .getAllRewardItems(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "pointCost")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(rewardItemService).getAllRewardItems(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Sort multiSort = Sort.by(
                    Sort.Order.desc("stock"),
                    Sort.Order.desc("pointCost"),
                    Sort.Order.asc("name")
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.getAllRewardItems(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .getAllRewardItems(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(rewardItemService).getAllRewardItems(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/items/search - When searching reward items")
    class WhenSearchingRewardItems {
        @Test
        @DisplayName("Should search reward items with name criteria")
        void shouldSearchRewardItemsWithNameCriteria() {
            String name = "p";
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                    1L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .searchRewardItems(name, null, null, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(rewardItemService).searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search reward items with point cost value range criteria")
        void shouldSearchRewardItemsWithPointCostValueRangeCriteria() {
            Integer minPointCost = 1;
            Integer maxPointCost = 5;
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "pointCost")),
                    1L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .searchRewardItems(null, minPointCost, maxPointCost, null, null,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "pointCost")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(rewardItemService).searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search reward items with stock value range criteria")
        void shouldSearchRewardItemsWithStockValueRangeCriteria() {
            Integer minStock = 20;
            Integer maxStock = 50;
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "stock")),
                    1L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .searchRewardItems(null, null, null, minStock, maxStock,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "stock")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(rewardItemService).searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search reward items with combined criteria")
        void shouldSearchRewardItemsWithCombinedCriteria() {
            String name = "";
            Integer minPointCost = 1;
            Integer maxPointCost = 5;
            Integer minStock = 20;
            Integer maxStock = 50;
            List<RewardItemDTO> rewardItems = List.of(
                    createRewardItemDTO(1L, "pencil", 3, 35)
            );
            Page<RewardItemDTO> rewardItemPage = new PageImpl<>(rewardItems,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                    1L);
            PagedResponseDTO<RewardItemDTO> expectedResponse = PagedResponseDTO.of(rewardItemPage);
            when(rewardItemService.searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class)))
                    .thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<RewardItemDTO>> response = rewardItemController
                    .searchRewardItems(name, minPointCost, maxPointCost, minStock, maxStock,
                            PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(rewardItemService).searchRewardItems(any(RewardItemSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/items/{id} - When retrieving reward item by ID")
    class WhenRetrievingRewardItemById {
        @Test
        @DisplayName("Should return reward item when found")
        void shouldReturnRewardItemWhenFound() {
            Long rewardItemId = 1L;
            RewardItemDTO rewardItem = createRewardItemDTO(rewardItemId, "pencil", 3, 35);
            when(rewardItemService.getRewardItemById(rewardItemId)).thenReturn(rewardItem);
            ResponseEntity<RewardItemDTO> response = rewardItemController.getRewardItemById(rewardItemId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(rewardItemId, response.getBody().getId());
            verify(rewardItemService).getRewardItemById(rewardItemId);
        }

        @Test
        @DisplayName("Should return 404 when reward item not found")
        void shouldReturn404WhenRewardItemNotFound() {
            Long rewardItemId = 9999L;
            when(rewardItemService.getRewardItemById(rewardItemId))
                    .thenThrow(new ResourceNotFoundException("Reward item not found with ID: " + rewardItemId));
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemController.getRewardItemById(rewardItemId));
            verify(rewardItemService).getRewardItemById(rewardItemId);
        }
    }

    @Nested
    @DisplayName("POST /api/items - When creating reward item")
    class WhenCreatingRewardItem {
        @Test
        @DisplayName("Should create new reward item and return 201 status")
        void shouldCreateNewRewardItemAndReturn201Status() {
            Long rewardItemId = 1L;
            String name = "test";
            Integer pointCost = 5;
            Integer stock = 60;
            RewardItemDTO rewardItemDTO = createRewardItemDTO(null, name, pointCost, stock);
            RewardItemDTO createdRewardItem = createRewardItemDTO(rewardItemId, name, pointCost, stock);
            when(rewardItemService.createRewardItem(rewardItemDTO)).thenReturn(createdRewardItem);
            ResponseEntity<RewardItemDTO> response = rewardItemController.createRewardItem(rewardItemDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(rewardItemId, response.getBody().getId());
            assertEquals(name, response.getBody().getName());
            assertEquals(pointCost, response.getBody().getPointCost());
            assertEquals(stock, response.getBody().getStock());
            verify(rewardItemService).createRewardItem(rewardItemDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{id} - When updating reward item")
    class WhenUpdatingRewardItem {
        @Test
        @DisplayName("Should update existing reward item and return 200 status")
        void shouldUpdateExistingRewardItemAndReturn200Status() {
            Long rewardItemId = 1L;
            String name = "test";
            Integer pointCost = 5;
            Integer stock = 60;
            RewardItemDTO rewardItemDTO = createRewardItemDTO(null, name, pointCost, stock);
            RewardItemDTO updatedRewardItem = createRewardItemDTO(rewardItemId, name, pointCost, stock);
            when(rewardItemService.updateRewardItem(rewardItemId, rewardItemDTO)).thenReturn(updatedRewardItem);
            ResponseEntity<RewardItemDTO> response = rewardItemController.updateRewardItem(rewardItemId, rewardItemDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(rewardItemId, response.getBody().getId());
            assertEquals(name, response.getBody().getName());
            assertEquals(pointCost, response.getBody().getPointCost());
            assertEquals(stock, response.getBody().getStock());
            verify(rewardItemService).updateRewardItem(rewardItemId, rewardItemDTO);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent reward item")
        void shouldReturn404WhenUpdatingNonexistentRewardItem() {
            Long rewardItemId = 9999L;
            RewardItemDTO rewardItemDTO = createRewardItemDTO(null, "nonexistent", 1, 1);
            when(rewardItemService.updateRewardItem(rewardItemId, rewardItemDTO))
                    .thenThrow(new ResourceNotFoundException("Reward item not found with ID: " + rewardItemId));
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemController.updateRewardItem(rewardItemId, rewardItemDTO));
            verify(rewardItemService).updateRewardItem(rewardItemId, rewardItemDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/items/{id} - When deleting reward item")
    class WhenDeletingRewardItem {
        @Test
        @DisplayName("Should delete reward item and return 204 status")
        void shouldDeleteRewardItemAndReturn204Status() {
            Long rewardItemId = 1L;
            ResponseEntity<Void> response = rewardItemController.deleteRewardItem(rewardItemId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(rewardItemService).deleteRewardItem(rewardItemId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent reward item")
        void shouldReturn404WhenDeletingNonexistentRewardItem() {
            Long rewardItemId = 999L;
            doThrow(new ResourceNotFoundException("Reward item not found with ID: " + rewardItemId))
                    .when(rewardItemService).deleteRewardItem(rewardItemId);
            assertThrows(ResourceNotFoundException.class,
                    () -> rewardItemController.deleteRewardItem(rewardItemId));
            verify(rewardItemService).deleteRewardItem(rewardItemId);
        }
    }
}
