package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StockServiceImpl}.
 * <p>Verifies stock management functionality including increment and decrement operations
 * with proper validation and exception handling.
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Successful stock decrement and increment</li>
 *     <li>Insufficient stock handling</li>
 *     <li>Item not found scenarios</li>
 * </ul>
 *
 * @see StockServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Unit Tests")
public class StockServiceTests {
    String INSUFFICIENT_STOCK_EXCEPTION_MESSAGE = "Insufficient stock to redeem this reward";
    String ITEM_NOT_FOUND_EXCEPTION_MESSAGE = "Reward item not found with ID: ";

    @Mock
    private RewardItemDAO rewardItemDAO;

    @InjectMocks
    private StockServiceImpl stockService;

    private RewardItem item;
    private final long itemId = 1L;

    @BeforeEach
    void setUp() {
        item = new RewardItem();
        item.setId(itemId);
        item.setName("Test item");
        item.setStock(5);
    }

    @Nested
    @DisplayName("When decrementing stock")
    class DecrementStock {
        @Test
        @DisplayName("should decrement stock successfully")
        void decrementStockSuccessfully() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.of(item));
            stockService.decrementStock(itemId);
            assertEquals(4, item.getStock());
            verify(rewardItemDAO).save(item);
        }

        @Test
        @DisplayName("should throw exception when stock is zero")
        void decrementStockInsufficient() {
            item.setStock(0);
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.of(item));
            InsufficientResourcesException ex = assertThrows(
                    InsufficientResourcesException.class,
                    () -> stockService.decrementStock(itemId)
            );
            assertEquals(ex.getMessage(), INSUFFICIENT_STOCK_EXCEPTION_MESSAGE);
            assertEquals(0, item.getStock());
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void decrementStockItemNotFound() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.decrementStock(itemId)
            );
            assertTrue(ex.getMessage().contains(ITEM_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }
    }

    @Nested
    @DisplayName("When incrementing stock")
    class IncrementStock {
        @Test
        @DisplayName("should increment stock successfully")
        void incrementStockSuccessfully() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.of(item));
            stockService.incrementStock(itemId);
            assertEquals(6, item.getStock());
            verify(rewardItemDAO).save(item);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void incrementStockItemNotFound() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.incrementStock(itemId)
            );
            assertTrue(ex.getMessage().contains(ITEM_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }
    }

    @Nested
    @DisplayName("When checking sufficient stock")
    class HasSufficientStock {
        @Test
        @DisplayName("should not throw when item has enough stock")
        void hasSufficientStock() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.of(item));
            assertDoesNotThrow(() -> stockService.hasSufficientStock(itemId));
            verify(rewardItemDAO).findById(itemId);
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
        }

        @Test
        @DisplayName("should throw when item does not have enough stock")
        void hasSufficientStockInsufficient() {
            item.setStock(0);
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.of(item));
            InsufficientResourcesException ex = assertThrows(
                    InsufficientResourcesException.class,
                    () -> stockService.hasSufficientStock(itemId)
            );
            assertEquals(ex.getMessage(), INSUFFICIENT_STOCK_EXCEPTION_MESSAGE);
            verify(rewardItemDAO).findById(itemId);
        }

        @Test
        @DisplayName("should throw when item not found")
        void hasSufficientStockItemNotFound() {
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.hasSufficientStock(itemId)
            );
            assertTrue(ex.getMessage().contains(ITEM_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(rewardItemDAO).findById(itemId);
        }
    }
}
