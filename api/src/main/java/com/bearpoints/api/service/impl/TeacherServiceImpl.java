package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.TeacherService;
import com.bearpoints.api.specification.TeacherSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherServiceImpl implements TeacherService {
    private final UserDAO userDAO;
    private final TeacherDAO teacherDAO;

    @Override
    public PagedResponseDTO<TeacherDTO> getAllTeachers(Pageable pageable) {
        log.debug("Retrieving all teachers with pagination: {}", pageable);
        Page<TeacherDTO> teacherPage = teacherDAO.findAll(pageable)
                .map(TeacherDTO::new);
        log.info("Retrieved {} teachers out of {} total",
                teacherPage.getNumberOfElements(),
                teacherPage.getTotalElements());
        return new PagedResponseDTO<>(teacherPage);
    }

    @Override
    public PagedResponseDTO<TeacherDTO> searchTeachers(TeacherSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching teachers with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided return all teachers
            return getAllTeachers(pageable);
        }
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<TeacherDTO> teacherPage = teacherDAO.findAll(spec, pageable).map(TeacherDTO::new);
        log.info("Found {} teachers matching search criteria", teacherPage.getNumberOfElements());
        return PagedResponseDTO.of(teacherPage);
    }

    @Override
    public TeacherDTO getTeacherById(Long id) {
        log.debug("Retrieving teacher by ID: {}", id);
        Teacher teacher = teacherDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        log.debug("Successfully retrieved teacher ID: {}", id);
        return new TeacherDTO(teacher);
    }

    @Override
    @Transactional
    public TeacherDTO createTeacher(TeacherDTO teacherDTO) {
        log.debug("Creating new teacher with email: {}", teacherDTO.getUser().getEmail());
        String email = teacherDTO.getUser().getEmail();
        Optional<Teacher> existingTeacher = teacherDAO.findByUserEmail(email);
        if (existingTeacher.isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setFirstName(teacherDTO.getUser().getFirstName());
        user.setLastName(teacherDTO.getUser().getLastName());
        user.setRole(Role.TEACHER);
        User savedUser = userDAO.save(user);
        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacher.setGrade(teacherDTO.getGrade());
        Teacher savedTeacher = teacherDAO.save(teacher);
        log.info("Successfully created teacher with ID: {}", savedTeacher.getId());
        return new TeacherDTO(savedTeacher);
    }

    @Override
    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        log.debug("Updating teacher with ID: {}", id);
        Teacher existingTeacher = teacherDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        User existingUser = existingTeacher.getUser();
        String newEmail = teacherDTO.getUser().getEmail();
        if (!existingUser.getEmail().equals(newEmail)) {
            Optional<Teacher> teacherWithEmail = teacherDAO.findByUserEmail(newEmail);
            if (teacherWithEmail.isPresent() && !teacherWithEmail.get().getId().equals(id)) {
                throw new DuplicateResourceException("A user with this email already exists");
            }
        }
        existingUser.setEmail(newEmail);
        existingUser.setFirstName(teacherDTO.getUser().getFirstName());
        existingUser.setLastName(teacherDTO.getUser().getLastName());
        existingTeacher.setGrade(teacherDTO.getGrade());
        Teacher updatedTeacher = teacherDAO.save(existingTeacher);
        log.info("Successfully updated teacher with ID: {}", id);
        return new TeacherDTO(updatedTeacher);
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        log.debug("Deleting teacher with ID: {}", id);
        Teacher teacher = teacherDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        teacherDAO.delete(teacher);
        log.info("Successfully deleted teacher with ID: {}", id);
    }
}
