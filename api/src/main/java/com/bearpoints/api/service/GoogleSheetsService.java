package com.bearpoints.api.service;

import com.bearpoints.api.dto.BatchUpdateRequest;

import java.io.IOException;
import java.util.List;

public interface GoogleSheetsService {
    int getRowCount(String sheetName) throws IOException;
    List<List<Object>> getSheetData(String sheetName) throws IOException;
    void appendToSheet(String sheetName, List<List<String>> data) throws IOException;
    void updateRow(String sheetName, int rowNumber, List<String> data) throws IOException;
    void clearSheet(String sheetName) throws IOException;
    void batchUpdate(String sheetName, List<BatchUpdateRequest> updates) throws IOException;
}
