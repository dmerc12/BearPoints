package com.bearpoints.api.service.impl;

import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.RewardItemDTO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.RewardItemService;
import com.bearpoints.api.specification.RewardItemSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link RewardItemService} for reward item management.
 *
 * @see RewardItemService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardItemServiceImpl implements RewardItemService {
    private final RewardItemDAO rewardItemDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<RewardItemDTO> getAllRewardItems(Pageable pageable) {
        log.debug("Retrieving all reward items with pagination: {}", pageable);
        Page<RewardItemDTO> rewardItemPage = rewardItemDAO.findAll(pageable).map(RewardItemDTO::new);
        log.info("Retrieved {} reward items", rewardItemPage.getTotalElements());
        return PagedResponseDTO.of(rewardItemPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<RewardItemDTO> searchRewardItems(RewardItemSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching reward items with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided, return all reward items
            return getAllRewardItems(pageable);
        }
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItemDTO> rewardItemPage = rewardItemDAO.findAll(spec, pageable).map(RewardItemDTO::new);
        log.info("Found {} reward items matching search criteria", rewardItemPage.getNumberOfElements());
        return PagedResponseDTO.of(rewardItemPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RewardItemDTO getRewardItemById(Long id) {
        log.debug("Retrieving reward item by ID: {}", id);
        RewardItem rewardItem = rewardItemDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + id));
        return new RewardItemDTO(rewardItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public RewardItemDTO createRewardItem(RewardItemDTO rewardItemDTO) {
        String name = rewardItemDTO.getName();
        log.debug("Creating reward item with name: {}", name);
        if (rewardItemDAO.findByName(name).isPresent()) {
            throw new DuplicateResourceException("A reward item with this name already exists");
        }
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName(name);
        rewardItem.setPointCost(rewardItemDTO.getPointCost());
        rewardItem.setStock(rewardItemDTO.getStock());
        RewardItem savedRewardItem = rewardItemDAO.save(rewardItem);
        log.info("Successfully created a reward item with ID: {}", savedRewardItem.getId());
        return new RewardItemDTO(savedRewardItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public RewardItemDTO updateRewardItem(Long id, RewardItemDTO rewardItemDTO) {
        log.debug("Updating reward item with ID: {}", id);
        RewardItem existingRewardItem = rewardItemDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + id));
        String newName = rewardItemDTO.getName();
        // Check for duplicate name if changed
        if (!existingRewardItem.getName().equals(newName)) {
            Optional<RewardItem> rewardItemWithName = rewardItemDAO.findByName(newName);
            if (rewardItemWithName.isPresent() && !rewardItemWithName.get().getId().equals(id)) {
                throw new DuplicateResourceException("A reward item with this name already exists");
            }
        }
        // Update reward item
        existingRewardItem.setName(newName);
        existingRewardItem.setPointCost(rewardItemDTO.getPointCost());
        existingRewardItem.setStock(rewardItemDTO.getStock());
        RewardItem updatedRewardItem = rewardItemDAO.save(existingRewardItem);
        log.info("Successfully updated a reward item with ID: {}", updatedRewardItem.getId());
        return new RewardItemDTO(updatedRewardItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteRewardItem(Long id) {
        log.debug("Deleting reward item with ID: {}", id);
        RewardItem rewardItem = rewardItemDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + id));
        rewardItemDAO.delete(rewardItem);
        log.info("Reward item '{}' (ID: {}) has been permanently deleted", rewardItem.getName(), id);
    }
}
