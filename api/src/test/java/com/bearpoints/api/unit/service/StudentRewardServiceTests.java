package com.bearpoints.api.unit.service;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.StudentRewardDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentRewardDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.StudentRewardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentRewardServiceImpl}.
 * <p>Verifies student reward management functionality including CRUD operations and
 * search with criteria.
 *
 * @see StudentRewardServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class StudentRewardServiceTests {
    @Mock
    private StudentRewardDAO studentRewardDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private RewardItemDAO rewardItemDAO;

    @InjectMocks
    private StudentRewardServiceImpl studentRewardService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Student student;
    private RewardItem item;
    private StudentReward studentReward;

    @BeforeEach
    public void setUp() {
        student = createStudent(1L);
        item = createRewardItem(1L, "Stickers");
        studentReward = createStudentReward(student, item);
    }

    @Nested
    @DisplayName("When retrieving all student rewards")
    class WhenRetrievingAllStudentRewards {
        @Test
        @DisplayName("Should retrieve all student rewards with pagination")
        void shouldRetrieveAllStudentRewardsWithPagination() {
            List<StudentReward> studentRewards = List.of(studentReward);
            Page<StudentReward> studentRewardPage = new PageImpl<>(studentRewards, pageable, 1L);
            when(studentRewardDAO.findAll(any(Pageable.class))).thenReturn(studentRewardPage);
            PagedResponseDTO<StudentRewardDTO> result = studentRewardService.getAllStudentRewards(pageable);
            assertNotNull(result);
            assertEquals(1L, result.getContent().size());
            assertEquals(1L, result.getTotalElements());
            verify(studentRewardDAO).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no student rewards exist")
        void shouldReturnEmptyPageWhenNoStudentRewardsExist() {
            Page<StudentReward> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(studentRewardDAO.findAll(any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<StudentRewardDTO> result = studentRewardService.getAllStudentRewards(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            verify(studentRewardDAO).findAll(pageable);
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching student rewards with criteria")
    class WhenSearchingStudentRewardsWithCriteria {
        @Test
        @DisplayName("Should search student rewards with student name criteria")
        void shouldSearchStudentRewardsWithStudentNameCriteria() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentName("ValidFirstName");
            List<StudentReward> studentRewards = List.of(studentReward);
            Page<StudentReward> studentRewardPage = new PageImpl<>(studentRewards, pageable, 1L);
            when(studentRewardDAO.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(studentRewardPage);
            PagedResponseDTO<StudentRewardDTO> result = studentRewardService.searchStudentRewards(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentRewardDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all student rewards with no criteria specified")
        void shouldReturnAllStudentRewardsWithNoCriteriaSpecified() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            List<StudentReward> studentRewards = List.of(studentReward);
            Page<StudentReward> studentRewardPage = new PageImpl<>(studentRewards, pageable, 1L);
            when(studentRewardDAO.findAll(any(Pageable.class))).thenReturn(studentRewardPage);
            PagedResponseDTO<StudentRewardDTO> result = studentRewardService.searchStudentRewards(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(studentRewardDAO).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("When retrieving student reward by ID")
    class WhenRetrievingStudentRewardById {
        @Test
        @DisplayName("Should return student reward by ID when found")
        void shouldReturnStudentRewardByIdWhenFound() {
            Long studentRewardId = studentReward.getId();
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            StudentRewardDTO result = studentRewardService.getStudentRewardById(studentRewardId);
            assertNotNull(result);
            assertEquals(studentRewardId, result.getId());
            assertEquals(student.getId(), result.getStudentId());
            assertEquals(item.getId(), result.getItemId());
            verify(studentRewardDAO).findById(studentRewardId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student reward not found by ID")
        void shouldThrowResourceNotFoundWhenStudentRewardNotFoundById() {
            Long studentRewardId = 9999L;
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.getStudentRewardById(studentRewardId));
            verify(studentRewardDAO).findById(studentRewardId);
        }
    }

    @Nested
    @DisplayName("When creating student reward")
    class WhenCreatingStudentReward {
        @Test
        @DisplayName("Should create new student reward successfully")
        void shouldCreateNewStudentRewardSuccessfully() {
            StudentRewardDTO createDTO = new StudentRewardDTO(null, student.getId(), item.getId(),
                    null, null, null, null);
            int expectedPoints = student.getPoints() - item.getPointCost();
            int expectedStock = item.getStock() - 1;
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(rewardItemDAO.findById(item.getId())).thenReturn(Optional.of(item));
            when(studentRewardDAO.save(any(StudentReward.class))).thenReturn(studentReward);
            when(studentDAO.save(any(Student.class))).thenReturn(student);
            StudentRewardDTO result = studentRewardService.createStudentReward(createDTO);
            assertEquals(expectedPoints, studentReward.getStudent().getPoints());
            assertEquals(expectedStock, studentReward.getRewardItem().getStock());
            assertNotNull(result);
            assertEquals(studentReward.getId(), result.getId());
            verify(studentDAO).findById(student.getId());
            verify(rewardItemDAO).findById(item.getId());
            verify(studentRewardDAO).save(any(StudentReward.class));
            verify(studentDAO).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw InsufficientResourcesException when student does not have enough points")
        void shouldThrowInsufficientPointsResourcesExceptionWhenStudentDoesNotHaveEnoughPoints() {
            student.setPoints(0);
            StudentRewardDTO createDTO = new StudentRewardDTO(null, student.getId(), item.getId(),
                    null, null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(rewardItemDAO.findById(item.getId())).thenReturn(Optional.of(item));
            assertThrows(InsufficientResourcesException.class,
                    () -> studentRewardService.createStudentReward(createDTO));
            verify(studentDAO).findById(student.getId());
            verify(rewardItemDAO).findById(item.getId());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
            verify(studentDAO, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw InsufficientResourcesException when reward item does not have enough stock")
        void shouldThrowInsufficientPointsResourcesExceptionWhenRewardItemDoesNotHaveEnoughStock() {
            item.setStock(0);
            StudentRewardDTO createDTO = new StudentRewardDTO(null, student.getId(), item.getId(),
                    null, null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(rewardItemDAO.findById(item.getId())).thenReturn(Optional.of(item));
            assertThrows(InsufficientResourcesException.class,
                    () -> studentRewardService.createStudentReward(createDTO));
            verify(studentDAO).findById(student.getId());
            verify(rewardItemDAO).findById(item.getId());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
            verify(studentDAO, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentNotFound() {
            Long studentId = 9999L;
            StudentRewardDTO createDTO = new StudentRewardDTO(null, studentId, item.getId(),
                    null, null, null, null);
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.createStudentReward(createDTO));
            verify(studentDAO).findById(studentId);
            verify(rewardItemDAO, never()).findById(anyLong());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when reward item not found")
        void shouldThrowResourceNotFoundExceptionWhenRewardItemNotFound() {
            Long itemId = 9999L;
            StudentRewardDTO createDTO = new StudentRewardDTO(null, student.getId(), itemId,
                    null, null, null, null);
            when(studentDAO.findById(student.getId())).thenReturn(Optional.of(student));
            when(rewardItemDAO.findById(itemId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.createStudentReward(createDTO));
            verify(studentDAO).findById(student.getId());
            verify(rewardItemDAO).findById(itemId);
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }
    }

    @Nested
    @DisplayName("When updating student reward")
    class WhenUpdatingStudentReward {
        @Test
        @DisplayName("Should update existing student reward when student changes")
        void shouldUpdateExistingStudentRewardWhenStudentChanges() {
            Long studentRewardId = studentReward.getId();
            Student otherStudent = createStudent(2L);
            otherStudent.setPoints(50);
            int originalStudentInitialPoints = student.getPoints();
            int originalItemInitialStock = item.getStock();
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, otherStudent.getId(), item.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(studentDAO.findById(otherStudent.getId())).thenReturn(Optional.of(otherStudent));
            when(studentRewardDAO.save(any(StudentReward.class))).thenReturn(studentReward);
            StudentRewardDTO result = studentRewardService.updateStudentReward(studentRewardId, updateDTO);
            assertNotNull(result);
            assertEquals(originalStudentInitialPoints + item.getPointCost(), student.getPoints());
            assertEquals(50 - item.getPointCost(), otherStudent.getPoints());
            assertEquals(originalItemInitialStock, item.getStock());
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).findById(otherStudent.getId());
            verify(studentRewardDAO).save(any(StudentReward.class));
            verify(studentDAO).save(student);
            verify(studentDAO).save(otherStudent);
        }

        @Test
        @DisplayName("Should update existing student reward when item changes")
        void shouldUpdateExistingStudentRewardWhenItemChanges() {
            Long studentRewardId = studentReward.getId();
            RewardItem otherItem = createRewardItem(2L, "Pencil");
            otherItem.setStock(10);
            otherItem.setPointCost(15);
            int originalStudentInitialPoints = student.getPoints();
            int originalItemInitialStock = item.getStock();
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, student.getId(), otherItem.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(rewardItemDAO.findById(otherItem.getId())).thenReturn(Optional.of(otherItem));
            when(studentRewardDAO.save(any(StudentReward.class))).thenReturn(studentReward);
            StudentRewardDTO result = studentRewardService.updateStudentReward(studentRewardId, updateDTO);
            assertNotNull(result);
            int expectedStudentPoints = originalStudentInitialPoints + item.getPointCost() - otherItem.getPointCost();
            assertEquals(expectedStudentPoints, student.getPoints());
            assertEquals(originalItemInitialStock + 1, item.getStock());
            assertEquals(10 - 1, otherItem.getStock());
            verify(studentRewardDAO).findById(studentRewardId);
            verify(rewardItemDAO).findById(otherItem.getId());
            verify(studentDAO).save(student);
            verify(rewardItemDAO).save(item);
            verify(rewardItemDAO).save(otherItem);
            verify(studentRewardDAO).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should update existing student reward when both student and item change")
        void shouldUpdateExistingStudentRewardWhenBothStudentAndItemChange() {
            Long studentRewardId = studentReward.getId();
            Student otherStudent = createStudent(2L);
            otherStudent.setPoints(50);
            RewardItem otherItem = createRewardItem(2L, "Pencil");
            otherItem.setStock(10);
            otherItem.setPointCost(15);
            int originalStudentInitialPoints = student.getPoints() + item.getPointCost();
            int newStudentsFinalPoints = otherStudent.getPoints() - otherItem.getPointCost();
            int newItemStock = otherItem.getStock() - 1;
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, otherStudent.getId(), otherItem.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(studentDAO.findById(otherStudent.getId())).thenReturn(Optional.of(otherStudent));
            when(rewardItemDAO.findById(otherItem.getId())).thenReturn(Optional.of(otherItem));
            when(studentRewardDAO.save(any(StudentReward.class))).thenReturn(studentReward);
            StudentRewardDTO result = studentRewardService.updateStudentReward(studentRewardId, updateDTO);
            assertNotNull(result);
            assertEquals(originalStudentInitialPoints, student.getPoints());
            assertEquals(newStudentsFinalPoints, otherStudent.getPoints());
            assertEquals(newItemStock, otherItem.getStock());
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).findById(otherStudent.getId());
            verify(rewardItemDAO).findById(otherItem.getId());
            verify(studentRewardDAO).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should handle nothing changing")
        void shouldHandleNothingChanging() {
            Long studentRewardId = studentReward.getId();
            int initialStudentPoints = student.getPoints();
            int initialItemStock = item.getStock();
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, student.getId(), item.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            StudentRewardDTO result = studentRewardService.updateStudentReward(studentRewardId, updateDTO);
            assertNotNull(result);
            assertEquals(initialStudentPoints, student.getPoints());
            assertEquals(initialItemStock, item.getStock());
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO, never()).findById(anyLong());
            verify(rewardItemDAO, never()).findById(anyLong());
            verify(studentDAO, never()).save(any(Student.class));
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw InsufficientResourcesException when student does not have enough points")
        void shouldThrowInsufficientPointsResourcesExceptionWhenStudentDoesNotHaveEnoughPoints() {
            Long studentRewardId = studentReward.getId();
            Student otherStudent = createStudent(2L);
            otherStudent.setPoints(5);
            RewardItem otherItem = createRewardItem(2L, "Pencil");
            otherItem.setPointCost(10);
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, otherStudent.getId(), otherItem.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(studentDAO.findById(otherStudent.getId())).thenReturn(Optional.of(otherStudent));
            when(rewardItemDAO.findById(otherItem.getId())).thenReturn(Optional.of(otherItem));
            assertThrows(InsufficientResourcesException.class,
                    () -> studentRewardService.updateStudentReward(studentRewardId, updateDTO));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).findById(otherStudent.getId());
            verify(rewardItemDAO).findById(otherItem.getId());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw InsufficientResourcesException when reward item does not have enough stock")
        void shouldThrowInsufficientPointsResourcesExceptionWhenRewardItemDoesNotHaveEnoughStock() {
            Long studentRewardId = studentReward.getId();
            Student otherStudent = createStudent(2L);
            otherStudent.setPoints(50);
            RewardItem otherItem = createRewardItem(2L, "Pencil");
            otherItem.setStock(0);
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, otherStudent.getId(), otherItem.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(studentDAO.findById(otherStudent.getId())).thenReturn(Optional.of(otherStudent));
            when(rewardItemDAO.findById(otherItem.getId())).thenReturn(Optional.of(otherItem));
            assertThrows(InsufficientResourcesException.class,
                    () -> studentRewardService.updateStudentReward(studentRewardId, updateDTO));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).findById(otherStudent.getId());
            verify(rewardItemDAO).findById(otherItem.getId());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentNotFound() {
            Long studentRewardId = studentReward.getId();
            Long otherStudentId = 9999L;
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, otherStudentId, item.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(studentDAO.findById(otherStudentId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.updateStudentReward(studentRewardId, updateDTO));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).findById(otherStudentId);
            verify(rewardItemDAO, never()).findById(anyLong());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when reward item not found")
        void shouldThrowResourceNotFoundExceptionWhenRewardItemNotFound() {
            Long studentRewardId = studentReward.getId();
            Long otherItemId = 9999L;
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, student.getId(), otherItemId,
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            when(rewardItemDAO.findById(otherItemId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.updateStudentReward(studentRewardId, updateDTO));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO, never()).findById(anyLong());
            verify(rewardItemDAO).findById(otherItemId);
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student reward not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentRewardNotFound() {
            Long studentRewardId = 9999L;
            StudentRewardDTO updateDTO = new StudentRewardDTO(studentRewardId, student.getId(), item.getId(),
                    null, null, null, null);
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.updateStudentReward(studentRewardId, updateDTO));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO, never()).findById(anyLong());
            verify(rewardItemDAO, never()).findById(anyLong());
            verify(studentRewardDAO, never()).save(any(StudentReward.class));
        }
    }

    @Nested
    @DisplayName("When deleting student reward")
    class WhenDeletingStudentReward {
        @Test
        @DisplayName("Should delete student reward successfully and reverse transaction")
        void shouldDeleteStudentRewardSuccessfullyAndReverseTransaction() {
            Long studentRewardId = studentReward.getId();
            int initialStudentPoints = student.getPoints();
            int initialItemStock = item.getStock();
            int itemPointCost = item.getPointCost();
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.of(studentReward));
            doNothing().when(studentRewardDAO).delete(studentReward);
            studentRewardService.deleteStudentReward(studentRewardId);
            assertEquals(initialStudentPoints + itemPointCost, student.getPoints());
            assertEquals(initialItemStock + 1, item.getStock());
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO).save(student);
            verify(rewardItemDAO).save(item);
            verify(studentRewardDAO).delete(studentReward);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when student reward not found")
        void shouldThrowResourceNotFoundExceptionWhenStudentRewardNotFound() {
            Long studentRewardId = 9999L;
            when(studentRewardDAO.findById(studentRewardId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> studentRewardService.deleteStudentReward(studentRewardId));
            verify(studentRewardDAO).findById(studentRewardId);
            verify(studentDAO, never()).save(any(Student.class));
            verify(rewardItemDAO, never()).save(any(RewardItem.class));
            verify(studentRewardDAO, never()).delete(any(StudentReward.class));
        }
    }

    private User createUser(Long id, Role role) {
        String firstName = "ValidFirstName";
        String lastName = "ValidLastName";
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName + lastName + id + "@okcps.org");
        user.setRole(role);
        return user;
    }

    private Student createStudent(Long id) {
        Long teacherId = id + 100;
        User teacherUser = createUser(teacherId, Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.FIRST);
        User studentUser = createUser(id, Role.STUDENT);
        Student student = new Student();
        student.setId(id);
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        student.setPoints(50);
        return student;
    }

    private RewardItem createRewardItem(Long id, String name) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setId(id);
        rewardItem.setName(name);
        rewardItem.setStock(100);
        rewardItem.setPointCost(8);
        return rewardItem;
    }

    private StudentReward createStudentReward(Student student, RewardItem item) {
        StudentReward studentReward = new StudentReward();
        studentReward.setId(1L);
        studentReward.setStudent(student);
        studentReward.setRewardItem(item);
        studentReward.setRedeemedAt(LocalDateTime.now());
        return studentReward;
    }
}
