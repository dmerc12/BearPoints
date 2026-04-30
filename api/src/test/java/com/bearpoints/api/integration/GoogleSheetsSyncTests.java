package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.GoogleSheetsSyncController;
import com.bearpoints.api.service.GoogleSheetsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for {@link GoogleSheetsSyncController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the Google Sheets synchronization endpoint, validating:
 * <ul>
 *     <li>Successful sync execution with proper response</li>
 *     <li>Security constraints and role-based access</li>
 *     <li>Error handling scenarios</li>
 *     <li>Service interaction verification</li>
 * </ul>
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive behavior type data</li>
 * </ul>
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@DisplayName("Google Sheets Sync Integration Tests")
public class GoogleSheetsSyncTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoogleSheetsService googleSheetsService;

    private static String baseUrl;

    private static final RequestPostProcessor ALLOWED_ROLES = user("admin").roles("ADMIN", "STAFF");
    private static final RequestPostProcessor DISALLOWED_ROLES = user("disallowed").roles("STUDENT", "TEACHER", "PARA");

    private static final List<String> SHEET_NAMES = Arrays.asList(
            "Users", "Teachers", "Students", "BehaviorTypes", "BragLogs", "RewardItems", "StudentRewards");

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/sync";
    }

    @BeforeEach
    void clearSheets() {
        for (String sheetName: SHEET_NAMES) {
            try {
                googleSheetsService.clearSheet(sheetName);
                log.info("Cleared sheet: {}", sheetName);
                List<List<String>> header = Collections.singletonList(getHeaderRowForSheet(sheetName));
                googleSheetsService.appendToSheet(sheetName, header);
            } catch (IOException e) {
                log.debug("Could not clear sheet {} (might not exist yet): {}", sheetName, e.getMessage());
            }
        }
    }

    private List<String> getHeaderRowForSheet(String sheetName) {
        return switch (sheetName) {
            case "Users" -> Arrays.asList("ID", "Email", "First Name", "Last Name", "Role");
            case "Teachers" -> Arrays.asList("ID", "Grade", "User ID");
            case "Students" -> Arrays.asList("ID", "Points", "Token", "User ID", "Teacher ID");
            case "BehaviorTypes" -> Arrays.asList("ID", "Name", "Point Value", "Active");
            case "BragLogs" -> Arrays.asList("ID", "Student ID", "Teacher ID", "Behaviors", "Points Generated",
                    "Submitter Name", "Submitter User ID", "Notes", "Timestamp");
            case "RewardItems" -> Arrays.asList("ID", "Name", "Point Cost", "Stock");
            case "StudentRewards" -> Arrays.asList("ID", "Redeemed At", "Student ID", "Reward Item ID");
            default -> Collections.emptyList();
        };
    }

    @Nested
    @DisplayName("POST /api/sync - Trigger full synchronization")
    class TriggerFullSync {
        @Test
        @DisplayName("accepts sync request from user with allowed role")
        void acceptsSyncRequestFromUserWithAllowedRole() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .with(csrf()).with(ALLOWED_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_PLAIN + ";charset=UTF-8"))
                    .andExpect(content().string("Google Sheets synchronization completed successfully"));
        }
    }

    @Nested
    @DisplayName("Security - Endpoint access control")
    class SecurityAccessControl {
        @Test
        @DisplayName("allows access for users with allowed role")
        void allowsAccessForAllowedRole() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .with(csrf()).with(ALLOWED_ROLES))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 403 for users with STUDENT or TEACHER roles")
        void returns403ForStudentOrTeacherRoles() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 403 when user is not authenticated")
        void returns403WhenUserNotAuthenticated() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
