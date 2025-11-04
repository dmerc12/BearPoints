package com.bearpoints.api.service;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.service.impl.BragLogServiceImpl;

/**
 * Represents service responsible for public brag log submissions.
 * <p>Implemented with {@link BragLogServiceImpl}
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public interface BragLogService {
    /** Service to assist in submitting brag logs */
    BragLog submitBragLog(BragLogRequest request);
}
