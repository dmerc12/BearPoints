package com.bearpoints.api.integration;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.controller.RewardItemController;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link RewardItemController}.
 * Extends {@link BaseIntegrationTest} for common test configuration.
 *
 * <p>Tests the complete reward item management flow from HTTP endpoint through service layer to
 * database, validating system behavior against production-like database environment with existing
 * {@link TestDataInitializer}.
 *
 * <p>Tests configuration:
 * <ul>
 *     <li>Uses PostgreSQL Testcontainers for realistic database testing</li>
 *     <li>Activates "test" profile for isolated test execution</li>
 *     <li>Configures security context with mock authentication</li>
 *     <li>Leverages application's test data initializer for comprehensive reward item data</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @see BaseIntegrationTest
 * @version 1.2
 * @author Dylan Mercer
 */
@DisplayName("Reward Item Integration Tests")
public class RewardItemTests extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RewardItemDAO rewardItemDAO;

    private static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = "/api/items";
    }

    @Nested
    @DisplayName("GET /api/items - Retrieve reward items")
    class GetAllRewardItems {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns paginated reward items with default parameters")
        void returnsPaginatedRewardItemsWithDefaults() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted results when sort parameter provided")
        void returnsSortedRewardItems() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("sort", "name,asc;pointCost,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns empty page when no reward items exist")
        void returnsEmptyPageWhenNoRewardItemsExist() throws Exception {
            mockMvc.perform(get(baseUrl)
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/items/search - Search reward items")
    class SearchRewardItems {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("no criteria returns all reward items")
        void noCriteriaReturnsAllRewardItems() throws Exception {
            mockMvc.perform(get(baseUrl + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by name criteria returns matching reward items")
        void byNameCriteriaReturnsMatchingRewardItems() throws Exception {
            String searchTerm = "R";
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(searchTerm))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by point cost range criteria returns matching reward items")
        void byPointCostRangeCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer minPointCost = 2;
            Integer maxPointCost = 50;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointCost", String.valueOf(minPointCost))
                            .param("maxPointCost", String.valueOf(maxPointCost)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointCost",
                            everyItem(allOf(greaterThanOrEqualTo(minPointCost), lessThanOrEqualTo(maxPointCost)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by min point cost criteria returns matching reward items")
        void byMinPointCostCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer minPointCost = 2;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minPointCost", String.valueOf(minPointCost)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointCost",
                            everyItem(greaterThanOrEqualTo(minPointCost))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by max point cost criteria returns matching reward items")
        void byMaxPointCostCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer maxPointCost = 50;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxPointCost", String.valueOf(maxPointCost)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].pointCost",
                            everyItem(lessThanOrEqualTo(maxPointCost))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by stock range criteria returns matching reward items")
        void byStockRangeCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer minStock = 1;
            Integer maxStock = 500;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minStock", String.valueOf(minStock))
                            .param("maxStock", String.valueOf(maxStock)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].stock",
                            everyItem(allOf(greaterThanOrEqualTo(minStock), lessThanOrEqualTo(maxStock)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by min stock criteria returns matching reward items")
        void byMinStockCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer minStock = 1;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("minStock", String.valueOf(minStock)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].stock",
                            everyItem(greaterThanOrEqualTo(minStock))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("by max stock criteria returns matching reward items")
        void byMaxStockCriteriaReturnsMatchingRewardItems() throws Exception {
            Integer maxStock = 500;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("maxStock", String.valueOf(maxStock)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].stock",
                            everyItem(lessThanOrEqualTo(maxStock))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with combined criteria returns matching reward items")
        void withCombinedCriteriaReturnsMatchingRewardItems() throws Exception {
            String name = "R";
            Integer minPointCost = 1;
            Integer maxPointCost = 500;
            Integer minStock = 1;
            Integer maxStock = 500;
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", name)
                            .param("minPointCost", String.valueOf(minPointCost))
                            .param("maxPointCost", String.valueOf(maxPointCost))
                            .param("minStock", String.valueOf(minStock))
                            .param("maxStock", String.valueOf(maxStock)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name",
                            everyItem(containsStringIgnoringCase(name))))
                    .andExpect(jsonPath("$.content[*].pointCost",
                            everyItem(allOf(greaterThanOrEqualTo(minPointCost), lessThanOrEqualTo(maxPointCost)))))
                    .andExpect(jsonPath("$.content[*].stock",
                            everyItem(allOf(greaterThanOrEqualTo(minStock), lessThanOrEqualTo(maxStock)))));
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("with non-matching criteria returns empty results")
        void withNonMatchingCriteriaReturnsEmptyResults() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns sorted search results when sort parameter provided")
        void returnsSortedSearchResultsWhenSortParameterProvided() throws Exception {
            mockMvc.perform(get(baseUrl + "/search")
                            .param("name", "")
                            .param("sort", "pointCost,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/items/{id} - Get reward item by ID")
    class GetRewardItemById {
        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns reward item when ID exists")
        void returnsRewardItemWhenIdExists() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.pointCost").exists())
                    .andExpect(jsonPath("$.stock").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "ADMIN", "STAFF"})
        @DisplayName("returns 404 when ID does not exist")
        void returns404WhenIdDoesNotExist() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", "9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Reward item not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/items - Create reward item")
    class CreateRewardItem {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("creates reward item with valid data")
        void createRewardItemWithValidData() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            Integer pointCost = 4;
            Integer stock = 44;
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": %s,
                        "stock": %s
                    }
                    """.formatted(uniqueName, pointCost, stock);
            mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rewardItemJson)
                        .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value(uniqueName))
                    .andExpect(jsonPath("$.pointCost").value(pointCost))
                    .andExpect(jsonPath("$.stock").value(stock));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 409 with duplicate name")
        void returns409WithDuplicateName() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": 8,
                        "stock": 40
                    }
                    """.formatted(existingRewardItem.get().getName());
                mockMvc.perform(post(baseUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(rewardItemJson)
                                .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A reward item with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank name")
        void returns400WithBlankName() throws Exception {
            String rewardItemJson = """
                    {
                        "name": "",
                        "pointCost": 8,
                        "stock": 40
                    }
                    """;
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with name too long")
        void returns400WithNameTooLong() throws Exception {
            String name = "A".repeat(51);
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": 8,
                        "stock": 40
                    }
                    """.formatted(name);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with point cost null")
        void returns400WithPointCostNull() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": "",
                        "stock": 40
                    }
                    """.formatted(uniqueName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with point cost below minimum")
        void returns400WithPointCostBelowMinimum() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": -1,
                        "stock": 40
                    }
                    """.formatted(uniqueName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with stock null")
        void returns400WithStockNull() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": 6,
                        "stock": ""
                    }
                    """.formatted(uniqueName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with stock below minimum")
        void returns400WithStockBelowMinimum() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": 6,
                        "stock": -1
                    }
                    """.formatted(uniqueName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Validation failed"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"TEACHER", "STUDENT", "STAFF"})
        @DisplayName("returns 403 when user is not admin")
        void returns403WhenUserIsNotAdmin() throws Exception {
            String uniqueName = "unique-name-" + System.currentTimeMillis();
            String rewardItemJson = """
                    {
                        "name": "%s",
                        "pointCost": 6,
                        "stock": 90
                    }
                    """.formatted(uniqueName);
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rewardItemJson)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{id} - Update reward item")
    class UpdateRewardItem {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates reward item with valid data")
        void updateRewardItemWithValidData() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String uniqueName = "new-unique-name-" + System.currentTimeMillis();
                Integer pointCost = 18;
                Integer stock = 3;
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": %s,
                            "stock": %s
                        }
                """.formatted(uniqueName, pointCost, stock);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(rewardItemId))
                        .andExpect(jsonPath("$.name").value(uniqueName))
                        .andExpect(jsonPath("$.pointCost").value(pointCost))
                        .andExpect(jsonPath("$.stock").value(stock));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("updates reward item when name is unchanged")
        void updateRewardItemWhenNameIsUnchanged() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String oldName = existingRewardItem.get().getName();
                Integer pointCost = 18;
                Integer stock = 3;
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": %s,
                            "stock": %s
                        }
                """.formatted(oldName, pointCost, stock);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(rewardItemId))
                        .andExpect(jsonPath("$.name").value(oldName))
                        .andExpect(jsonPath("$.pointCost").value(pointCost))
                        .andExpect(jsonPath("$.stock").value(stock));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 409 when updating to existing reward item's name")
        void returns409WhenUpdatingToExistingRewardItemName() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            Optional<RewardItem> otherRewardItem = rewardItemDAO.findAll(PageRequest.of(1, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent() && otherRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String otherItemName = otherRewardItem.get().getName();
                Integer pointCost = 18;
                Integer stock = 3;
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": %s,
                            "stock": %s
                        }
                """.formatted(otherItemName, pointCost, stock);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message")
                                .value("A reward item with this name already exists"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with blank name")
        void returns400WithBlankName() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String updateJson = """
                        {
                            "name": "",
                            "pointCost": 18,
                            "stock": 3
                        }
                """;
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with name too long")
        void returns400WithNameTooLong() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String name = "a".repeat(51);
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": 18,
                            "stock": 3
                        }
                """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with point cost null")
        void returns400WithPointCostNull() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String name = "new-unique-name-" + System.currentTimeMillis();
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": "",
                            "stock": 3
                        }
                """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with point cost below minimum")
        void returns400WithPointCostBelowMinimum() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String name = "new-unique-name-" + System.currentTimeMillis();
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": -1,
                            "stock": 3
                        }
                """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with stock null")
        void returns400WithStockNull() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String name = "new-unique-name-" + System.currentTimeMillis();
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": 8,
                            "stock": ""
                        }
                """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 400 with stock below minimum")
        void returns400WithStockBelowMinimum() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String name = "new-unique-name-" + System.currentTimeMillis();
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": 8,
                            "stock": -1
                        }
                """.formatted(name);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("Validation failed"))
                        .andExpect(jsonPath("$.timestamp").exists());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when updating non-existent reward item")
        void returns404WhenUpdatingNonExistentRewardItem() throws Exception {
            Long rewardItemId = 9999L;
            String name = "new-unique-name-" + System.currentTimeMillis();
            String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": 20,
                            "stock": 3
                        }
                """.formatted(name);
            mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Reward item not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "STAFF"})
        @DisplayName("returns 403 when user is not admin")
        void returns403WhenUserIsNotAdmin() throws Exception {
            Optional<RewardItem> existingRewardItem = rewardItemDAO.findAll(PageRequest.of(0, 1))
                    .stream().findFirst();
            if (existingRewardItem.isPresent()) {
                Long rewardItemId = existingRewardItem.get().getId();
                String uniqueName = "new-unique-name-" + System.currentTimeMillis();
                Integer pointCost = 18;
                Integer stock = 3;
                String updateJson = """
                        {
                            "name": "%s",
                            "pointCost": %s,
                            "stock": %s
                        }
                """.formatted(uniqueName, pointCost, stock);
                mockMvc.perform(put(baseUrl + "/{id}", rewardItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/items/{id} - Delete reward item")
    class DeleteRewardItem {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deletes reward item and returns 204")
        void deletesRewardItemAndReturns204() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when deleting non-existent reward item")
        void returns404WhenDeletingNonExistentRewardItem() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 9999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Reward item not found with ID: 9999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = {"STUDENT", "TEACHER", "STAFF"})
        @DisplayName("returns 403 when user is not admin")
        void returns403WhenUserIsNotAdmin() throws Exception {
            mockMvc.perform(delete(baseUrl + "/{id}", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
