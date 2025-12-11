package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentDTO;
import com.bearpoints.api.dto.StudentSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.StudentService;
import com.bearpoints.api.specification.StudentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link StudentService} for student management.
 *
 * @see StudentService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final UserDAO userDAO;

    @Override
    public PagedResponseDTO<StudentDTO> getAllStudents(Pageable pageable) {
        log.debug("Retrieving all students with pagination: {}", pageable);
        Page<StudentDTO> studentPage = studentDAO.findAll(pageable).map(StudentDTO::new);
        log.info("Retrieved {} students", studentPage.getNumberOfElements());
        return PagedResponseDTO.of(studentPage);
    }

    @Override
    public PagedResponseDTO<StudentDTO> searchStudents(StudentSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching students with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided, return all students
            return getAllStudents(pageable);
        }
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<StudentDTO> studentPage = studentDAO.findAll(spec, pageable).map(StudentDTO::new);
        log.info("Found {} students matching search criteria", studentPage.getNumberOfElements());
        return PagedResponseDTO.of(studentPage);
    }

    @Override
    public PagedResponseDTO<StudentDTO> getClassRoomLeaderboard(Long teacherId, Pageable pageable) {
        log.debug("Retrieving classroom leaderboard for teacher ID: {}", teacherId);
        Teacher teacher = teacherDAO.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));
        Page<StudentDTO> leaderboardPage = studentDAO.findByTeacherOrderByPointsDesc(teacher, pageable).map(StudentDTO::new);
        log.info("Retrieved {} students for classroom leaderboard", leaderboardPage.getNumberOfElements());
        return PagedResponseDTO.of(leaderboardPage);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        log.debug("Retrieving student by ID: {}", id);
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return new StudentDTO(student);
    }

    @Override
    public StudentDTO getStudentByToken(String token) {
        log.debug("Retrieving student by token: {}", token);
        Student student = studentDAO.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with token: " + token));
        return new StudentDTO(student);
    }

    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        log.debug("Creating student with email: {}", studentDTO.getUser().getEmail());
        String email = studentDTO.getUser().getEmail();
        if (studentDAO.findByUserEmail(email).isPresent()) {
            throw new DuplicateResourceException("A student with this email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setFirstName(studentDTO.getUser().getFirstName());
        user.setLastName(studentDTO.getUser().getLastName());
        user.setRole(Role.STUDENT);
        User savedUser = userDAO.save(user);
        Teacher teacher = teacherDAO.findById(studentDTO.getTeacher().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + studentDTO.getTeacher().getId()));
        Student student = new Student();
        student.setUser(savedUser);
        student.setTeacher(teacher);
        student.generateToken();
        Student savedStudent = studentDAO.save(student);
        log.info("Successfully created student with ID: {}", savedStudent.getId());
        return new StudentDTO(savedStudent);
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        log.debug("Updating student with ID: {}", id);
        Student existingStudent = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        User existingUser = existingStudent.getUser();
        String newEmail = studentDTO.getUser().getEmail();
        // Check for duplicate email if changed
        if (!existingUser.getEmail().equals(newEmail)) {
            Optional<Student> studentWithEmail = studentDAO.findByUserEmail(newEmail);
            if (studentWithEmail.isPresent() && !studentWithEmail.get().getId().equals(id)) {
                throw new DuplicateResourceException("A student with this email already exists");
            }
        }
        // Update user
        existingUser.setEmail(newEmail);
        existingUser.setFirstName(studentDTO.getUser().getFirstName());
        existingUser.setLastName(studentDTO.getUser().getLastName());
        // Update teacher if changed
        if (!existingStudent.getTeacher().getId().equals(studentDTO.getTeacher().getId())) {
            Teacher newTeacher = teacherDAO.findById(studentDTO.getTeacher().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + studentDTO.getTeacher().getId()));
            existingStudent.setTeacher(newTeacher);
        }
        Student updatedStudent = studentDAO.save(existingStudent);
        log.info("Successfully updated student with ID: {}", updatedStudent.getId());
        return new StudentDTO(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        log.debug("Deleting student with ID: {}", id);
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        studentDAO.delete(student);
        log.info("Successfully deleted student with ID: {}", id);
    }
}
