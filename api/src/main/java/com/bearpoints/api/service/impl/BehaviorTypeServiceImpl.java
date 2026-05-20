package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.criteria.BehaviorTypeSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.BehaviorTypeService;
import com.bearpoints.api.specification.BehaviorTypeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link BehaviorTypeService} for behavior type management.
 *
 * @see BehaviorTypeService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorTypeServiceImpl implements BehaviorTypeService {
    private final BehaviorTypeDAO behaviorTypeDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<BehaviorTypeDTO> getAllBehaviorTypes(Pageable pageable) {
        log.debug("Retrieving all behavior types with pagination: {}", pageable);
        Page<BehaviorTypeDTO> behaviorTypePage = behaviorTypeDAO.findAll(pageable).map(BehaviorTypeDTO::new);
        log.info("Retrieved {} behavior types", behaviorTypePage.getNumberOfElements());
        return PagedResponseDTO.of(behaviorTypePage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponseDTO<BehaviorTypeDTO> searchBehaviorTypes(BehaviorTypeSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching behavior types with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided, return all behavior types
            return getAllBehaviorTypes(pageable);
        }
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorTypeDTO> behaviorTypePage = behaviorTypeDAO.findAll(spec, pageable).map(BehaviorTypeDTO::new);
        log.info("Found {} behavior types matching search criteria", behaviorTypePage.getNumberOfElements());
        return PagedResponseDTO.of(behaviorTypePage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BehaviorTypeDTO getBehaviorTypeById(Long id) {
        log.debug("Retrieving behavior type by ID: {}", id);
        BehaviorType behaviorType = behaviorTypeDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + id));
        return new BehaviorTypeDTO(behaviorType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BehaviorTypeDTO createBehaviorType(BehaviorTypeDTO behaviorTypeDTO) {
        log.debug("Creating behavior type with name: {}", behaviorTypeDTO.getName());
        String name = behaviorTypeDTO.getName();
        if (behaviorTypeDAO.findByName(name).isPresent()) {
            throw new DuplicateResourceException("A behavior type with this name already exists");
        }
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName(name);
        behaviorType.setActive(behaviorTypeDTO.getActive());
        behaviorType.setPointValue(behaviorTypeDTO.getPointValue());
        BehaviorType savedBehaviorType = behaviorTypeDAO.save(behaviorType);
        log.info("Successfully created a behavior type with ID: {}", savedBehaviorType.getId());
        return new BehaviorTypeDTO(savedBehaviorType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BehaviorTypeDTO updateBehaviorType(Long id, BehaviorTypeDTO behaviorTypeDTO) {
        log.debug("Updating behavior type with ID: {}", id);
        BehaviorType existingBehaviorType = behaviorTypeDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + id));
        String newName = behaviorTypeDTO.getName();
        // Check for duplicate name if changed
        if (!existingBehaviorType.getName().equals(newName)) {
            Optional<BehaviorType> behaviorTypeWithName = behaviorTypeDAO.findByName(newName);
            if (behaviorTypeWithName.isPresent() && !behaviorTypeWithName.get().getId().equals(id)) {
                throw new DuplicateResourceException("A behavior type with this name already exists");
            }
        }
        // Update behavior type
        existingBehaviorType.setName(newName);
        existingBehaviorType.setActive(behaviorTypeDTO.getActive());
        existingBehaviorType.setPointValue(behaviorTypeDTO.getPointValue());
        BehaviorType updatedBehaviorType = behaviorTypeDAO.save(existingBehaviorType);
        log.info("Successfully updated a behavior type with ID: {}", updatedBehaviorType.getId());
        return new BehaviorTypeDTO(updatedBehaviorType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteBehaviorType(Long id) {
        log.debug("Deleting behavior type with ID: {}", id);
        BehaviorType behaviorType = behaviorTypeDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + id));
        // Check if behavior type is used in any brag logs
        boolean isUsed = behaviorTypeDAO.isBehaviorTypeUsed(id);
        if (isUsed) {
            // Behavior type is used - soft delete (deactivate)
            behaviorType.setActive(false);
            behaviorTypeDAO.save(behaviorType);
            log.warn("Behavior type '{}' (ID: {}) is used in brag logs and has been deactivated",
                    behaviorType.getName(), id);
        } else {
            // Behavior type is not used - hard delete
            behaviorTypeDAO.delete(behaviorType);
            log.info("Behavior type'{}' (ID: {}) has been permanently deleted", behaviorType.getName(), id);
        }
    }
}
