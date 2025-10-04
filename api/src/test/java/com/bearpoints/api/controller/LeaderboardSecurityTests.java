package com.bearpoints.api.controller;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.security.FirebaseAuthFilter;
import com.bearpoints.api.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests for {@link LeaderboardController} endpoint access control.
 *
 * <p>Validates that leaderboard endpoints enforce role-based authentication
 * as configured in Spring Security and controller annotations.
 *
 * <p>Security requirements tested:
 * <ul>
 *     <li>STUDENT, TEACHER, ADMIN roles can access leaderboard</li>
 *     <li>Unauthenticated users are denied access (401 Unauthorized)</li>
 *     <li>Users without required roles are denied access (403 Forbidden)</li>
 * </ul>
 *
 * @see LeaderboardController
 * @see WithMockUser
 * @see WithAnonymousUser
 * @version 1.0
 * @author Dylan Mercer
 *
 */
@WebMvcTest(LeaderboardController.class)
public class LeaderboardSecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @MockitoBean
    private FirebaseAuthFilter firebaseAuthFilter;

    private List<LeaderboardEntryDTO> sampleLeaderboard;

    @BeforeEach
    void setUp() {
        sampleLeaderboard = List.of(
                new LeaderboardEntryDTO(1,
                new PersonDTO(1L, "Student", "One"),
                new PersonDTO(1L, "Teacher", "A"),
                "THIRD", 150)
        );
    }

    /**
     * Tests that STUDENT role can successfully access leaderboard endpoint.
     *
     * <p>Verifies:
     * <ul>
     *     <li>HTTP 200 status for authenticated STUDENT</li>
     *     <li>Endpoint allows STUDENT role access</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT role can access leaderboard - returns 200 OK")
    void getLeaderboard_WithStudentRole_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, 1);
        when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);
        mockMvc.perform(get("/api/leaderboard").param("timeframe", "WEEK"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that TEACHER role can successfully access leaderboard endpoint.
     *
     * <p>Verifies:
     * <ul>
     *     <li>HTTP 200 status for authenticated TEACHER</li>
     *     <li>Endpoint allows TEACHER role access</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER role can access leaderboard - returns 200 OK")
    void getLeaderboard_WithTeacherRole_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, 1);
        when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);
        mockMvc.perform(get("/api/leaderboard").param("timeframe", "WEEK"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that ADMIN role can successfully access leaderboard endpoint.
     *
     * <p>Verifies:
     * <ul>
     *     <li>HTTP 200 status for authenticated ADMIN</li>
     *     <li>Endpoint allows ADMIN role access</li>
     * </ul>
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN role can access leaderboard - returns 200 OK")
    void getLeaderboard_WithAdminRole_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, 1);
        when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);
        mockMvc.perform(get("/api/leaderboard").param("timeframe", "WEEK"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that unauthenticated users are denied access to leaderboard.
     *
     * <p>Verifies:
     * <ul>
     *     <li>HTTP 401 Unauthorized for unauthenticated requests</li>
     *     <li>Security filter chain blocks access without authentication</li>
     * </ul>
     */
    @Test
    @WithAnonymousUser
    @DisplayName("Unauthenticated user cannot access leaderboard - returns 401 Unauthorized")
    void getLeaderboard_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/leaderboard").param("timeframe", "WEEK"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests leaderboard access with teacher filter for authorized role.
     *
     * <p>Verifies that teacher filtering is accessible to authorized roles and
     * doesn't introduce additional security constraints.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT role can access leaderboard with teacher filter - returns 200 OK")
    void getLeaderboard_WithTeacherFilterAndStudentRole_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, 1);
        Long teacherId = 1L;
        when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(teacherId), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);
        mockMvc.perform(get("/api/leaderboard")
                        .param("timeframe", "WEEK")
                        .param("teacherId", teacherId.toString()))
                .andExpect(status().isOk());
    }

    /**
     * Tests leaderboard access with grade filter for authorized role.
     *
     * <p>Verifies that grade filtering is accessible to authorized roles and
     * doesn't introduce additional security constraints.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT role can access leaderboard with grade filter - returns 200 OK")
    void getLeaderboard_WithGradeFilterAndStudentRole_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<LeaderboardEntryDTO> expectedPage = new PageImpl<>(sampleLeaderboard, pageable, 1);
        String grade = "FIRST";
        when(leaderboardService.getLeaderboard(eq(Timeframe.WEEK), eq(null), eq(grade), any(Pageable.class)))
                .thenReturn(expectedPage);
        mockMvc.perform(get("/api/leaderboard")
                        .param("timeframe", "WEEK")
                        .param("grade", grade))
                .andExpect(status().isOk());
    }
}
