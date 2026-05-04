import { useAppDispatch, useAppSelector, triggerSync, clearSyncStatus } from '../../store';
import { type UseSyncReturn } from './UseSyncReturn';
import { useCallback } from 'react';

export function useSync(): UseSyncReturn {
    const dispatch = useAppDispatch();
    const { syncing, success, error, lastSynced } = useAppSelector(
        (state) => state.sync);

    const handleSync = useCallback(async () => {
        await dispatch(triggerSync()).unwrap();
    }, [dispatch]);

    const clearStatus = useCallback(() => {
        dispatch(clearSyncStatus());
    }, [dispatch]);

    return {
        syncing,
        success,
        error,
        lastSynced,
        handleSync,
        clearStatus,
    };
}
