package com.bearpoints.api.service;

import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.service.impl.GoogleSheetsServiceImpl;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for interacting with Google Sheets API.
 * <p>Defines operations for:
 * <ul>
 *     <li>Retrieving sheet metadata and content</li>
 *     <li>Modifying sheet data (appending, updating, clearing)</li>
 *     <li>Batch processing operations</li>
 * </ul>
 * <p>Implemented by {@link GoogleSheetsServiceImpl}
 *
 * @see GoogleSheetsServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
public interface GoogleSheetsService {
    /**
     * Retrieves the number of rows in a specified sheet.
     *
     * @param sheetName name of the sheet to inspect
     * @return number of rows in the sheet (0 for empty sheets)
     * @throws IOException if API communication fails
     */
    int getRowCount(String sheetName) throws IOException;

    /**
     * Retrieves all data from a specified sheet.
     *
     * @param sheetName name of the sheet to retrieve
     * @return 2D list of sheet data (rows x columns), empty list for empty sheets
     * @throws IOException if API communication fails
     */
    List<List<Object>> getSheetData(String sheetName) throws IOException;

    /**
     * Appends multiple rows to the end of a sheet.
     *
     * @param sheetName name of the target sheet
     * @param data rows to append (list of string lists)
     * @throws IOException if API communication fails
     */
    void appendToSheet(String sheetName, List<List<String>> data) throws IOException;

    /**
     * Updates a single row in a sheet.
     *
     * @param sheetName name of the target sheet
     * @param rowNumber 1-based row index to update
     * @param data cell values for the row
     * @throws IOException if API communication fails
     */
    void updateRow(String sheetName, int rowNumber, List<String> data) throws IOException;

    /**
     * Clears all content from a sheet while preserving formatting.
     *
     * @param sheetName name of the sheet to clear
     * @throws IOException if API communication fails
     */
    void clearSheet(String sheetName) throws IOException;

    /**
     * Performs batch updates on multiple rows in a single API call.
     *
     * @param sheetName name of the target sheet
     * @param updates list of update requests (row number + data)
     * @throws IOException if API communication fails
     */
    void batchUpdate(String sheetName, List<BatchUpdateRequest> updates) throws IOException;
}
