export interface UseSyncReturn {
    syncing: boolean;
    success: boolean;
    error: string | null;
    lastSynced: string | null;
    handleSync: () => Promise<void>;
    clearStatus: () => void;
}
