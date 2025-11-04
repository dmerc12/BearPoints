package com.bearpoints.api.service;

import com.bearpoints.api.service.impl.GoogleSheetsSyncServiceImpl;

/**
 * Service interface for synchronizing application data with Google Sheets.
 * <p>Defines operations for:
 * <ul>
 *     <li>Full data synchronization between database and Google Sheets</li>
 *     <li>Scheduled execution via cron job</li>
 * </ul>
 * <p>Implemented by {@link GoogleSheetsSyncServiceImpl}
 *
 * @see GoogleSheetsSyncServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
public interface GoogleSheetsSyncService {
    /**
     * Performs full synchronization of all application data with Google Sheets.
     * <p>Synchronizes:
     * <ul>
     *     <li>Users</li>
     *     <li>Teachers</li>
     *     <li>Students</li>
     *     <li>BehaviorTypes</li>
     *     <li>BragLogs</li>
     *     <li>RewardItems</li>
     *     <li>StudentRewards</li>
     * </ul>
     * <p>Features:
     * <ul>
     *     <li>Bidirectional synchronization (DB <-> Sheets)</-></li>
     *     <li>Batch processing with chunking</li>
     *     <li>Exponential backoff for API quota management</li>
     *     <li>Daily quota checks</li>
     * </ul>
     */
    void syncAllData();
}
