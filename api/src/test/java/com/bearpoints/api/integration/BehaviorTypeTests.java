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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link BehaviorTypeController} utilizing Testcontainers with PostgreSQL and
 * comprehensive test data initialization.
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
 * @see MockMvc
 * @author Dylan Mercer
 */
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Behavior Type Integration Tests")
public class BehaviorTypeTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/behavior-types";
    }

    @Nested
    @DisplayName("GET /behavior-types - Retrieve behavior types")
    class GetAllBehaviorTypes {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns paginated behavior types with default parameters")
        void returnsPaginatedBehaviorTypesWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Nested
        @DisplayName("GET /behavior-types - Sort direction edge cases")
        class SortDirectionTests {
            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort asc parameters provided")
            void returnsSortedBehaviorTypesAsc() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].name").exists());
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort desc parameters provided")
            void returnsSortedBehaviorTypesDesc() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "name,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].name").exists());
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when no sort direction parameter is provided")
            void returnsSortedBehaviorTypesNoDirection() throws Exception {
                mockMvc.perform(get(baseUrl)
                                .param("sort", "name"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].name").exists());
            }
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns empty page when no behavior types exist")
        void returnsEmptyPageWhenNoBehaviorTypesExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /behavior-types/search - Search behavior types")
    class SearchBehaviorTypes {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("no criteria returns all behavior types")
        void noCriteriaReturnsAllBehaviorTypes() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by name returns matching behavior types")
        void searchByName_ReturnsMatchingBehaviorTypes() throws Exception {
            String searchTerm = "B";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by active returns matching behavior types")
        void searchByActive_ReturnsMatchingBehaviorTypes() throws Exception {
            Boolean searchTerm = true;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("active", String.valueOf(searchTerm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].active",
                            everyItem(equalTo(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("by point value range returns matching behavior types")
        void searchByPointValueRange_ReturnsMatchingBehaviorTypes() throws Exception {
            Integer minPointValue = 2;
            Integer maxPointValue = 5;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointValue", String.valueOf(minPointValue))
                            .param("maxPointValue", String.valueOf(maxPointValue)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(allOf(greaterThanOrEqualTo(minPointValue), lessThanOrEqualTo(maxPointValue)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
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
                            .param("maxPointValue", String.valueOf(maxPointValue)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(searchTerm))))
                    .andExpect(jsonPath("$.content[*].active",
                            everyItem(equalTo(activeStatus))))
                    .andExpect(jsonPath("$.content[*].pointValue",
                            everyItem(allOf(greaterThanOrEqualTo(minPointValue), lessThanOrEqualTo(maxPointValue)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("with non-matching criteria returns empty results")
        void searchWithNonMatchingCriteria_ReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Nested
        @DisplayName("GET /behavior-types/search - Sort direction edge cases")
        class SortDirectionTests {
            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort asc parameters provided")
            void returnsSortedBehaviorTypesAsc() throws Exception {
                String searchTerm = "B";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("name", searchTerm)
                                .param("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].name",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when sort desc parameters provided")
            void returnsSortedBehaviorTypesDesc() throws Exception {
                String searchTerm = "B";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("name", searchTerm)
                                .param("sort", "name,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].name",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }

            @Test
            @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
            @DisplayName("returns sorted results when no sort direction parameter is provided")
            void returnsSortedBehaviorTypesNoDirection() throws Exception {
                String searchTerm = "B";
                mockMvc.perform(get(baseUrl + "/search")
                                .param("name", searchTerm)
                                .param("sort", "name"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].name",
                                everyItem(containsStringIgnoringCase(searchTerm))));
            }
        }
    }

    @Nested
    @DisplayName("GET /behavior-types/{id} - Get behavior type by ID")
    class GetBehaviorTypeById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns behavior type when ID exists")
        void returnsBehaviorType_whenIdExists() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.pointValue").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404_whenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /behavior-types - Create behavior type")
    class CreateBehaviorType {
        @Test
        @WithMockUser(roles = "ADMIN")
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
                        .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value(uniqueName))
                    .andExpect(jsonPath("$.pointValue").value(pointValue))
                    .andExpect(jsonPath("$.active").value(active));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A behavior type with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Validation failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER"})        @DisplayName("returns 403 when user is not ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
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
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /behavior-types/{id} - Update behavior type")
    class UpdateBehaviorType {
        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(behaviorTypeId))
                        .andExpect(jsonPath("$.name").value(uniqueName))
                        .andExpect(jsonPath("$.pointValue").value(pointValue))
                        .andExpect(jsonPath("$.active").value(active));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(behaviorTypeId))
                        .andExpect(jsonPath("$.name").value(name))
                        .andExpect(jsonPath("$.pointValue").value(pointValue))
                        .andExpect(jsonPath("$.active").value(active));
            }
        }



        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A behavior type with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Validation failed")))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER"})
        @DisplayName("returns 403 when user is not ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
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
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /behavior-types/{id} - Delete behavior type")
    class DeleteBehaviorType {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deletes behavior type and returns 204")
        void deletesBehaviorType_andReturns204() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-existent behavior type")
        void returns404_whenDeletingNonExistentBehaviorType() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Behavior type not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());

        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER"})
        @DisplayName("returns 403 when user is not ADMIN")
        void returns403_whenUserIsNotAdmin() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
