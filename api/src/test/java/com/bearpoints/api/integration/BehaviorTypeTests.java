package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.BehaviorTypeController;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.entity.BehaviorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for {@link BehaviorTypeController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete behavior type management flow from HTTP endpoint through service layer to
 * database, validating system behavior against production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing </li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive behavior type data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 2.0
 * @author Dylan Mercer
 */
@DisplayName("Behavior Type Integration Tests")
public class BehaviorTypeTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    private static String baseUrl;

    private static final RequestPostProcessor WRITE_ROLES = user("admin").roles("ADMIN", "STAFF");
    private static final RequestPostProcessor DISALLOWED_ROLES = user("disallowed").roles("STUDENT", "TEACHER", "PARA");
    private static final RequestPostProcessor READ_ROLES = user("any").roles("STUDENT", "TEACHER", "ADMIN", "STAFF", "PARA");

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/behaviors";
    }

    @Nested
    @DisplayName("GET /api/behaviors - Retrieve behavior types")
    class GetAllBehaviorTypes {
        @Test
        @DisplayName("returns paginated behavior types with default parameters")
        void returnsPaginatedBehaviorTypesWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedBehaviorTypes() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "name,asc;pointValue,desc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("returns empty page when no behavior types exist")
        void returnsEmptyPageWhenNoBehaviorTypesExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/behaviors/search - Search behavior types")
    class SearchBehaviorTypes {
        @Test
        @DisplayName("no criteria returns all behavior types")
        void noCriteriaReturnsAllBehaviorTypes() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("by name returns matching behavior types")
        void searchByName_ReturnsMatchingBehaviorTypes() throws Exception {
            String searchTerm = "B";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", searchTerm)
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @DisplayName("by active returns matching behavior types")
        void searchByActive_ReturnsMatchingBehaviorTypes() throws Exception {
            Boolean searchTerm = true;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("active", String.valueOf(searchTerm))
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].active",
                            everyItem(equalTo(searchTerm))));
        }

        @Test
        @DisplayName("by point value range returns matching behavior types")
        void searchByPointValueRange_ReturnsMatchingBehaviorTypes() throws Exception {
            Integer minPointValue = 2;
            Integer maxPointValue = 5;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointValue", String.valueOf(minPointValue))
                            .param("maxPointValue", String.valueOf(maxPointValue))
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(allOf(greaterThanOrEqualTo(minPointValue), lessThanOrEqualTo(maxPointValue)))));
        }

        @Test
        @DisplayName("by min point value returns matching behavior types")
        void searchByMinPointValue_ReturnsMatchingBehaviorTypes() throws Exception {
            Integer minPointValue = 2;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointValue", String.valueOf(minPointValue))
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(greaterThanOrEqualTo(minPointValue))));
        }

        @Test
        @DisplayName("by max point value returns matching behavior types")
        void searchByMaxPointValue_ReturnsMatchingBehaviorTypes() throws Exception {
            Integer maxPointValue = 5;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxPointValue", String.valueOf(maxPointValue))
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(lessThanOrEqualTo(maxPointValue))));
        }

        @Test
        @DisplayName("with combined criteria returns matching behavior types")
        void searchWithCombinedCriteria_ReturnsMatchingBehaviorTypes() throws Exception {
            String searchTerm = "B";
            Boolean activeStatus = true;
            Integer minPointValue = 2;
            Integer maxPointValue = 5;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", searchTerm)
                            .param("active", String.valueOf(activeStatus))
                            .param("minPointValue", String.valueOf(minPointValue))
                            .param("maxPointValue", String.valueOf(maxPointValue))
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(searchTerm))))
                    .andExpect(jsonPath("$.content[*].active",
                            everyItem(equalTo(activeStatus))))
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(allOf(greaterThanOrEqualTo(minPointValue), lessThanOrEqualTo(maxPointValue)))));
        }

        @Test
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_ReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "non-existent")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "")
                            .param("sort", "pointValue,desc")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/behaviors/{id} - Get behavior type by ID")
    class GetBehaviorTypeById {
        @Test
        @DisplayName("returns behavior type when ID exists")
        void returnsBehaviorType_whenIdExists() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "1")
                            .with(READ_ROLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.pointValue").exists());
        }

        @Test
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999")
                            .with(READ_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/behaviors - Create behavior type")
    class CreateBehaviorType {
        @Test
        @DisplayName("creates behavior type with valid data")
        void createBehaviorType_withValidData() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            Integer pointValue = 2;
            Boolean active = true;
            String behaviorTypeJson = """
                    {
                        "name": "%s",
                        "pointValue": %s,
                        "active": %s
                    }
                    """.formatted(uniqueName, pointValue, active);
            mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(behaviorTypeJson)
                        .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value(uniqueName))
                    .andExpect(jsonPath("$.pointValue").value(pointValue))
                    .andExpect(jsonPath("$.active").value(active));
        }

        @Test
        @DisplayName("returns 409 with duplicate name")
        void returns409_withDuplicateName() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                String behaviorTypeJson = """
                        {
                            "name": "%s",
                            "pointValue": 1,
                            "active": true
                        }
                        """.formatted(existingBehaviorType.get().getName());
                mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A behavior type with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 with blank name")
        void returns400_withBlankName() throws Exception {
            String name = "";
            String behaviorTypeJson = """
                    {
                        "name": "%s",
                        "pointValue": 1,
                        "active": true
                    }
                    """.formatted(name);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 with name too long")
        void returns400_withNameTooLong() throws Exception {
            String name = "a".repeat(51);
            String behaviorTypeJson = """
                    {
                        "name": "%s",
                        "pointValue": 1,
                        "active": true
                    }
                    """.formatted(name);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 with point value below minimum")
        void returns400_withPointValueBelowMin() throws Exception {
            Integer pointValue = 0;
            String behaviorTypeJson = """
                    {
                        "name": "test",
                        "pointValue": %s,
                        "active": true
                    }
                    """.formatted(pointValue);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 400 with point value above maximum")
        void returns400_withPointValueAboveMax() throws Exception {
            Integer pointValue = 6;
            String behaviorTypeJson = """
                    {
                        "name": "test",
                        "pointValue": %s,
                        "active": true
                    }
                    """.formatted(pointValue);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 403 when user has disallowed role")
        void returns403_whenUserHasDisallowedRole() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            Integer pointValue = 2;
            Boolean active = true;
            String behaviorTypeJson = """
                    {
                        "name": "%s",
                        "pointValue": %s,
                        "active": %s
                    }
                    """.formatted(uniqueName, pointValue, active);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(behaviorTypeJson)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/behaviors/{id} - Update behavior type")
    class UpdateBehaviorType {
        @Test
        @DisplayName("updates behavior type with valid data")
        void updateBehaviorType_withValidData() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                String uniqueName = "new-unique-name-" + System.currentTimeMillis();
                Integer pointValue = 3;
                Boolean active = false;
                String updateJson = """
                    {
                        "name": "%s",
                        "pointValue": %s,
                        "active": %s
                    }
                    """.formatted(uniqueName, pointValue, active);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(behaviorTypeId))
                        .andExpect(jsonPath("$.name").value(uniqueName))
                        .andExpect(jsonPath("$.pointValue").value(pointValue))
                        .andExpect(jsonPath("$.active").value(active));
            }
        }

        @Test
        @DisplayName("updates behavior type when name is unchanged")
        void updateBehaviorType_whenNameUnchanged() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                String name = existingBehaviorType.get().getName();
                Integer pointValue = 3;
                Boolean active = false;
                String updateJson = """
                    {
                        "name": "%s",
                        "pointValue": %s,
                        "active": %s
                    }
                    """.formatted(name, pointValue, active);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(behaviorTypeId))
                        .andExpect(jsonPath("$.name").value(name))
                        .andExpect(jsonPath("$.pointValue").value(pointValue))
                        .andExpect(jsonPath("$.active").value(active));
            }
        }



        @Test
        @DisplayName("returns 409 when updating to existing behavior type's name")
        void returns409_whenUpdatingToExistingName() throws Exception {
            Optional<BehaviorType> behaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(2, 1))
                    .stream().findFirst();
            if (behaviorType.isPresent() && existingBehaviorType.isPresent()) {
                Long behaviorTypeId = behaviorType.get().getId();
                String updateJson = """
                    {
                        "name": "%s",
                        "pointValue": 4,
                        "active": false
                    }
                    """.formatted(existingBehaviorType.get().getName());
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A behavior type with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 with blank name")
        void returns400_withBlankName() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                String name = "";
                String updateJson = """
                    {
                        "name": "%s",
                        "pointValue": 4,
                        "active": false
                    }
                    """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 with name too long")
        void returns400_withNameTooLong() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                String name = "a".repeat(51);
                String updateJson = """
                    {
                        "name": "%s",
                        "pointValue": 4,
                        "active": false
                    }
                    """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 with point value below minimum")
        void returns400_withPointValueBelowMin() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                Integer pointValue = 0;
                String updateJson = """
                    {
                        "name": "test",
                        "pointValue": %s,
                        "active": false
                    }
                    """.formatted(pointValue);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 400 with point value above maximum")
        void returns400_withPointValueAboveMax() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                Integer pointValue = 6;
                String updateJson = """
                    {
                        "name": "test",
                        "pointValue": %s,
                        "active": false
                    }
                    """.formatted(pointValue);
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(WRITE_ROLES))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @DisplayName("returns 404 when updating non-existent behavior type")
        void returns404_whenUpdatingNonExistentBehaviorType() throws Exception {
            Long behaviorTypeId = 9999L;
            String updateJson = """
                    {
                        "name": "test",
                        "pointValue": 4,
                        "active": false
                    }
                    """;
            mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns 403 when user has disallowed role")
        void returns403_whenUserHasDisallowedRole() throws Exception {
            Optional<BehaviorType> existingBehaviorType = behaviorTypeDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingBehaviorType.isPresent()) {
                Long behaviorTypeId = existingBehaviorType.get().getId();
                String updateJson = """
                    {
                        "name": "test",
                        "pointValue": 3,
                        "active": false
                    }
                    """;
                mockMvc.perform(put(baseUrl + "/{id}", behaviorTypeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()).with(DISALLOWED_ROLES))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/behaviors/{id} - Delete behavior type")
    class DeleteBehaviorType {
        @Test
        @DisplayName("deletes behavior type and returns 204")
        void deletesBehaviorType_andReturns204() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when deleting non-existent behavior type")
        void returns404_whenDeletingNonExistentBehaviorType() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()).with(WRITE_ROLES))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @DisplayName("returns 403 when user has disallowed role")
        void returns403_whenUserHasDisallowedRole() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()).with(DISALLOWED_ROLES))
                    .andExpect(status().isForbidden());
        }
    }
}
