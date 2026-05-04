import { triggerSync as triggerSyncApi, type SyncState, type SyncResponse } from '../../services';
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const initialState: SyncState = {
    syncing: false,
    success: false,
    error: null,
    lastSynced: null,
};

export const triggerSync = createAsyncThunk<SyncResponse, void>(
    'sync/triggerSync',
    async (_, { signal, rejectWithValue }) => {
        try {
            const result = await triggerSyncApi(signal);
            return { message: result, timestamp: new Date().toISOString() };
        } catch (error: unknown) {
            const err = error as { response?: { data?: string }; message?: string };
            return rejectWithValue(err.response?.data || err.message || 'Sync failed');
        }
    }
);

const syncSlice = createSlice({
    name: 'sync',
    initialState,
    reducers: {
        clearSyncStatus: (state) => {
            state.success = false;
            state.error = null;
        },
        resetSync: () => initialState,
    },
    extraReducers: (builder) => {
        builder
            .addCase(triggerSync.pending, (state) => {
                state.syncing = true;
                state.success = false;
                state.error = null;
            })
            .addCase(triggerSync.fulfilled, (state, action) => {
                state.syncing = false;
                state.success = true;
                state.error = null;
                state.lastSynced = action.payload.timestamp;
            })
            .addCase(triggerSync.rejected, (state, action) => {
                state.syncing = false;
                state.success = false;
                state.error = action.payload as string || 'Sync failed';
            });
    },
});

export const { clearSyncStatus, resetSync } = syncSlice.actions;
export default syncSlice.reducer;
