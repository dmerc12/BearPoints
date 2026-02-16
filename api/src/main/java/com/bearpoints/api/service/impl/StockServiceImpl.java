package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link StockService} using {@link RewardItemDAO}.
 * <p>Performs stock adjustments within a transaction, ensuring data consistency.
 * All operations validate input and throw appropriate exceptions.
 *
 * @see StockService
 * @version 1.1
 * @author Dylan Mercer
 */@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
     private final RewardItemDAO rewardItemDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementStock(Long itemId) {
        RewardItem item = getRewardItemById(itemId);
        checkSufficientStock(item.getStock(), item.getName());
        item.setStock(item.getStock() - 1);
        rewardItemDAO.save(item);
        log.debug("Decremented stock for item ID: {}, new stock: {}", itemId, item.getStock());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementStock(Long itemId) {
        RewardItem item = getRewardItemById(itemId);
        item.setStock(item.getStock() + 1);
        rewardItemDAO.save(item);
        log.debug("Incremented stock for item ID: {}, new stock: {}", itemId, item.getStock());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hasSufficientStock(Long itemId) {
        RewardItem item = getRewardItemById(itemId);
        checkSufficientStock(item.getStock(), item.getName());
        log.debug("Item {} has sufficient stock ({} >= 1)", itemId, item.getStock());
    }

    private RewardItem getRewardItemById(Long itemId) {
        return rewardItemDAO.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + itemId));
    }

    private void checkSufficientStock(int stock, String itemName) {
        if (stock < 1) {
            throw new InsufficientResourcesException("Insufficient stock for item: " + itemName);
        }
    }
}
