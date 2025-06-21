package com.bearpoints.api.service;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;

public interface BragLogService {
    BragLog submitBearBrag(BragLogRequest request);
}
