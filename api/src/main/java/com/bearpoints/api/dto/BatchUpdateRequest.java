package com.bearpoints.api.dto;

import java.util.List;

public record BatchUpdateRequest(int rowNumber, List<String> data) {
}
