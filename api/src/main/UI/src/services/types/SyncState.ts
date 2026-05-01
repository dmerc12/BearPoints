export interface SyncState {
    syncing: boolean;
    success: boolean;
    error: string | null;
    lastSynced: string | null;
}
