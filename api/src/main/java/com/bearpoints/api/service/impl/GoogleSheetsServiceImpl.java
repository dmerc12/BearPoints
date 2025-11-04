package com.bearpoints.api.service.impl;

import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.service.GoogleSheetsService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for interacting with Google Sheets API.
 * Handles operations such as reading sheet data, appending rows, updating cells,
 * clearing sheets, and batch updates.
 *
 * <p>Uses Google Service Account credentials for authentication.
 *
 * @see GoogleSheetsService
 * @version 1.0
 * @author Dylan Mercer
 */
@Service
public class GoogleSheetsServiceImpl implements GoogleSheetsService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Value("${google.service.account.key}")
    private String serviceAccountKey;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private Sheets sheets;

    /**
     * Initializes Google Sheets API client using service account credentials.
     *
     * @throws GeneralSecurityException if there's a security-related error
     * @throws IOException if there's an I/O error reading credentials
     */
    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(serviceAccountKey.getBytes(StandardCharsets.UTF_8))
        ).createScoped(SCOPES);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        this.sheets = new Sheets.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName("BearPoints API").build();
    }

    /**
     * Retrieves row count for a specific sheet.
     *
     * @param sheetName name of the sheet
     * @return number of rows in the sheet
     * @throws IOException if API call fails
     */
    @Override
    public int getRowCount(String sheetName) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, sheetName).execute();
        return response.getValues() != null ? response.getValues().size() : 0;
    }

    /**
     * Retrieves all data from a sheet.
     *
     * @param sheetName name of the sheet
     * @return 2D list representing sheet data (rows x columns)
     * @throws IOException if API call fails
     */
    @Override
    public List<List<Object>> getSheetData(String sheetName) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, sheetName).execute();
        return response.getValues() != null ? response.getValues() : new ArrayList<>();
    }

    /**
     * Appends multiple rows to a sheet.
     *
     * @param sheetName name of the sheet
     * @param data rows to append (list of string lists)
     * @throws IOException if API call fails
     */
    @Override
    public void appendToSheet(String sheetName, List<List<String>> data) throws IOException {
        List<List<Object>> convertedData = data.stream()
                .map(row -> new ArrayList<Object>(row))
                .collect(Collectors.toList());
        ValueRange body = new ValueRange().setValues(convertedData);
        sheets.spreadsheets().values()
                .append(spreadsheetId, sheetName, body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    /**
     * Updates a single row in a sheet.
     *
     * @param sheetName name of the sheet
     * @param rowNumber 1-based row index
     * @param data cell values for the row
     * @throws IOException if API call fails
     */
    @Override
    public void updateRow(String sheetName, int rowNumber, List<String> data) throws IOException {
        String range = sheetName + "!A" + rowNumber + ":Z" + rowNumber;
        List<List<Object>> values = Collections.singletonList(new ArrayList<>(data));
        ValueRange body = new ValueRange().setValues(values);
        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    /**
     * Clears all data from a sheet.
     *
     * @param sheetName name of the sheet
     * @throws IOException if API call fails
     */
    @Override
    public void clearSheet(String sheetName) throws IOException {
        ClearValuesRequest request = new ClearValuesRequest();
        sheets.spreadsheets().values()
                .clear(spreadsheetId, sheetName, request)
                .execute();
    }

    /**
     * Performs batch updates on multiple rows.
     *
     * @param sheetName name of the sheet
     * @param updates list of update requests
     * @throws IOException if API call fails
     */
    @Override
    public void batchUpdate(String sheetName, List<BatchUpdateRequest> updates) throws IOException {
        List<ValueRange> data = new ArrayList<>();
        for (BatchUpdateRequest update : updates) {
            ValueRange valueRange = new ValueRange()
                    .setRange(sheetName + "!A" + update.rowNumber() + ":Z" + update.rowNumber())
                    .setValues(Collections.singletonList(new ArrayList<>(update.data())));
            data.add(valueRange);
        }
        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(data);
        sheets.spreadsheets().values()
                .batchUpdate(spreadsheetId, batchBody)
                .execute();
    }
}
