package com.bearpoints.api.service;

import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.service.impl.GoogleSheetsServiceImpl;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoogleSheetsService} implementation and functionality.
 * <p>Tests cover all operations including:
 * <ul>
 *     <li>Client initialization with Google credentials</li>
 *     <li>Reading sheet data and row counts</li>
 *     <li>Writing operations (appending, updating, clearing)</li>
 *     <li>Batch update operations</li>
 *     <li>Edge cases and error scenarios</li>
 * </ul>
 * <p>Uses Mockito for mocking Google Sheets API dependencies to ensure isolated unit tests.
 *
 * @see GoogleSheetsService
 * @see GoogleSheetsServiceImpl
 * @version 1.2
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class GoogleSheetsServiceTests {
    @Mock
    private Sheets sheets;

    @Mock
    private GoogleCredentials credentials;

    @Mock
    private Sheets.Spreadsheets spreadsheets;

    @Mock
    private Sheets.Spreadsheets.Values values;

    @Mock
    private Sheets.Spreadsheets.Values.Get getRequest;

    @Mock
    private Sheets.Spreadsheets.Values.Append appendRequest;

    @Mock
    private Sheets.Spreadsheets.Values.Update updateRequest;

    @Mock
    private Sheets.Spreadsheets.Values.Clear clearRequest;

    @Mock
    private Sheets.Spreadsheets.Values.BatchUpdate batchUpdateRequest;

    @InjectMocks
    private GoogleSheetsServiceImpl googleSheetsService;

    private final String spreadsheetId = "test-spreadsheet-id";
    private final String sheetName = "TestSheet";

    /**
     * Sets up common test environment before each test.
     * <ul>
     *     <li>Injects test spreadsheet ID</li>
     *     <li>Configures lenient API client stubs</li>
     * </ul>
     */
    @BeforeEach
    public void setup() throws GeneralSecurityException, IOException {
        ReflectionTestUtils.setField(googleSheetsService, "spreadsheetId", spreadsheetId);
        lenient().when(sheets.spreadsheets()).thenReturn(spreadsheets);
        lenient().when(spreadsheets.values()).thenReturn(values);
    }

    @Nested
    @DisplayName("Client Initialization Tests")
    class InitTests {
        /**
         * Tests successful client initialization with valid credentials.
         * <p>Verifies:
         * <ul>
         *     <li>No exceptions thrown during initialization</li>
         *     <li>Sheets client instance is created</li>
         * </ul>
         */
        @Test
        @DisplayName("Successfully creates Sheets client")
        public void createsSheetsClient() {
            try (MockedStatic<GoogleNetHttpTransport> mockTransport = mockStatic(GoogleNetHttpTransport.class);
                 MockedStatic<GoogleCredentials> mockedCredentials = mockStatic(GoogleCredentials.class)) {
                NetHttpTransport httpTransport = mock(NetHttpTransport.class);
                mockTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(httpTransport);
                mockedCredentials.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                        .thenReturn(credentials);
                when(credentials.createScoped(anyList())).thenReturn(credentials);
                String testKey = "{\"type\":\"service_account\"}";
                ReflectionTestUtils.setField(googleSheetsService, "serviceAccountKey", testKey);
                assertDoesNotThrow(() -> googleSheetsService.init());
                Sheets sheetsInstance = (Sheets) ReflectionTestUtils.getField(googleSheetsService, "sheets");
                assertNotNull(sheetsInstance);
            }
        }
    }

    /**
     * Tests for row count retrieval operations.
     * <p>Covers various data scenarios including:
     * <ul>
     *     <li>Sheets with data rows</li>
     *     <li>Empty responses</li>
     *     <li>Null data responses</li>
     * </ul>
     */
    @Nested
    @DisplayName("Row Count Retrieval Tests")
    class GetRowCountTests {
        /**
         * Tests row count with populated sheet data.
         * <p>Verifies:
         * <ul>
         *     <li>Correct row count is returned</li>
         *     <li>API method is called with correct parameters</li>
         * </ul>
         */
        @Test
        @DisplayName("Returns correct count for populated sheet")
        public void returnsCorrectCount() throws IOException {
            ValueRange response = new ValueRange().setValues(Arrays.asList(
                    Arrays.asList("Header1", "Header2"),
                    Arrays.asList("Data1", "Data2")
            ));
            when(values.get(spreadsheetId, sheetName)).thenReturn(getRequest);
            when(getRequest.execute()).thenReturn(response);
            int count = googleSheetsService.getRowCount(sheetName);
            assertEquals(2, count);
            verify(values).get(spreadsheetId, sheetName);
        }

        /**
         * Tests row count with null response data:
         * <p>Verifies:
         * <ul>
         *     <li>0 is returned for null data</li>
         *     <li>API method is called correctly</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles null response by returning 0")
        public void nullResponse_Returns0() throws IOException {
            ValueRange response = new ValueRange().setValues(null);
            when(values.get(spreadsheetId, sheetName)).thenReturn(getRequest);
            when(getRequest.execute()).thenReturn(response);
            int count = googleSheetsService.getRowCount(sheetName);
            assertEquals(0, count);
            verify(values).get(spreadsheetId, sheetName);
        }
    }

    /**
     * Tests for sheet data retrieval operations.
     * <p>Covers various data scenarios including:
     * <ul>
     *     <li>Sheets with data rows</li>
     *     <li>Empty responses</li>
     *     <li>Null data responses</li>
     * </ul>
     */
    @Nested
    @DisplayName("Sheet Data Retrieval Tests")
    class GetSheetDataTests {
        /**
         * Tests data retrieval with populated sheet.
         * <p>Verifies:
         * <ul>
         *     <li>Correct data structure is returned </li>
         *     <li>API method is called with correct parameters</li>
         * </ul>
         */
        @Test
        @DisplayName("Returns correct data for populated sheet")
        public void returnsData() throws IOException {
            ValueRange response = new ValueRange().setValues(Arrays.asList(
                    Arrays.asList("A1", "B1"),
                    Arrays.asList("A2", "B2")
            ));
            when(values.get(spreadsheetId, sheetName)).thenReturn(getRequest);
            when(getRequest.execute()).thenReturn(response);
            List<List<Object>> data = googleSheetsService.getSheetData(sheetName);
            assertEquals(2, data.size());
            verify(values).get(spreadsheetId, sheetName);
        }

        /**
         * Tests data retrieval with null response data.
         * <p>Verifies:
         * <ul>
         *     <li>Empty list is returned for null data</li>
         *     <li>API method is called correctly</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles null response by returning empty list")
        public void nullResponse_ReturnsEmpty() throws IOException {
            ValueRange response = new ValueRange().setValues(null);
            when(values.get(spreadsheetId, sheetName)).thenReturn(getRequest);
            when(getRequest.execute()).thenReturn(response);
            List<List<Object>> data = googleSheetsService.getSheetData(sheetName);
            assertTrue(data.isEmpty());
            verify(values).get(spreadsheetId, sheetName);
        }
    }

    /**
     * Tests for sheet append operations.
     * <p>Verifies proper handling of row insertion.
     */
    @Nested
    @DisplayName("Sheet Append Operation Tests")
    class AppendToSheetTests {
        /**
         * Tests successful row appending.
         * <p>Verifies:
         * <ul>
         *     <li>API append method is called with correct parameters</li>
         *     <li>Execution completes without errors</li>
         * </ul>
         */
        @Test
        @DisplayName("Successfully appends rows to sheet")
        public void executesCorrectly() throws IOException {
            List<List<String>> data = Arrays.asList(
                    Arrays.asList("Row1Col1", "Row1Col2"),
                    Arrays.asList("Row2Col1", "Row2Col2")
            );
            when(values.append(eq(spreadsheetId), eq(sheetName), any(ValueRange.class))).thenReturn(appendRequest);
            when(appendRequest.setValueInputOption(anyString())).thenReturn(appendRequest);
            when(appendRequest.setInsertDataOption(anyString())).thenReturn(appendRequest);
            googleSheetsService.appendToSheet(sheetName, data);
            verify(appendRequest).execute();
        }
    }

    /**
     * Tests for row update operations.
     * <p>Verifies proper handling of single row updates.
     */
    @Nested
    @DisplayName("Row Update Operation Tests")
    class UpdateRowTests {
        /**
         * Tests successful row update.
         * <p>Verifies:
         * <ul>
         *     <li>API update method is called with correct parameters</li>
         *     <li>Value input option is set correctly</li>
         *     <li>Execution completes without errors</li>
         * </ul>
         */
        @Test
        @DisplayName("Successfully updates row in sheet")
        public void executesCorrectly() throws IOException {
            List<String> rowData = Arrays.asList("Col1", "Col2");
            when(values.update(eq(spreadsheetId), anyString(), any(ValueRange.class)))
                    .thenReturn(updateRequest);
            when(updateRequest.setValueInputOption(eq("USER_ENTERED"))).thenReturn(updateRequest);
            googleSheetsService.updateRow(sheetName, 2, rowData);
            verify(updateRequest).setValueInputOption("USER_ENTERED");
            verify(updateRequest).execute();
        }
    }

    /**
     * Tests for sheet clear operations.
     * <p>Verifies proper handling of sheet clearing.
     */
    @Nested
    @DisplayName("Sheet Clear Operation Tests")
    class ClearSheetTests {
        /**
         * Tests successful sheet clearing.
         * <p>Verifies:
         * <ul>
         *     <li>API clear method is called with correct parameters</li>
         *     <li>Execution completes without errors</li>
         * </ul>
         */
        @Test
        @DisplayName("Successfully clears sheet contents")
        public void executesCorrectly() throws IOException {
            when(values.clear(eq(spreadsheetId), eq(sheetName), any(ClearValuesRequest.class))).thenReturn(clearRequest);
            googleSheetsService.clearSheet(sheetName);
            verify(clearRequest).execute();
        }
    }

    /**
     * Tests for batch update operations.
     * <p>Verifies proper handling of multiple row updates.
     */
    @Nested
    @DisplayName("Batch Update Operation Tests")
    class BatchUpdateTests {
        /**
         * Tests successful batch update.
         * <p>Verifies:
         * <ul>
         *     <li>API batch update method is called with correct parameters</li>
         *     <li>Execution completes without errors</li>
         * </ul>
         */
        @Test
        @DisplayName("Successfully executes batch updates")
        public void updatesCorrectly() throws IOException {
            List<BatchUpdateRequest> updates = Arrays.asList(
                    new BatchUpdateRequest(1, Arrays.asList("A1", "B1")),
                    new BatchUpdateRequest(2, Arrays.asList("A2", "B2"))
            );
            when(values.batchUpdate(eq(spreadsheetId), any(BatchUpdateValuesRequest.class))).thenReturn(batchUpdateRequest);
            googleSheetsService.batchUpdate(spreadsheetId, updates);
            verify(batchUpdateRequest).execute();
        }
    }
}
