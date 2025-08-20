import { getBragLogs, createBragLog, updateBragLog, deleteBragLog } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedBragLogs, BragLog } from '../../services/types';
import {RootState} from "../index.ts";

interface BragLogsState {
    bragLogs: BragLog[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalBragLogs: number;
    };
    lastFetched: number | null;
}

const initialState: BragLogsState = {
    bragLogs: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalBragLogs: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchBragLogs = createAsyncThunk(
    'bragLogs/fetchBragLogs',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.bragLogs.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                bragLogs: state.bragLogs.bragLogs,
                totalPages: state.bragLogs.pagination.totalPages,
                totalBragLogs: state.bragLogs.pagination.totalBragLogs
            };
        }
        return await getBragLogs(params.page, params.size, signal);
    }
);

export const addBragLog = createAsyncThunk(
    'bragLogs/addBragLog',
    async (bragLogData: Partial<BragLog>, { signal }) => {
        return await createBragLog(bragLogData, signal);
    }
);

export const modifyBragLog = createAsyncThunk(
    'bragLogs/modifyBragLog',
    async ({ id, bragLogData }: { id: number, bragLogData: Partial<BragLog> }, { signal }) => {
        return await updateBragLog(id, bragLogData, signal);
    }
);

export const removeBragLog = createAsyncThunk(
    'bragLogs/removeBragLog',
    async (id: number, { signal }) => {
        await deleteBragLog(id, signal);
        return id;
    }
);

const bragLogsSlice = createSlice({
    name: 'bragLogs',
    initialState,
    reducers: {
        clearBragLogsError: (state) => {
            state.error = null;
        },
        resetBragLogs: () => initialState,
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchBragLogs.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBragLogs.fulfilled, (state, action: PayloadAction<PaginatedBragLogs>) => {
                state.loading = false;
                state.bragLogs = action.payload.bragLogs;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalBragLogs: action.payload.totalBragLogs
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchBragLogs.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch bragLogs';
            })
            .addCase(addBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addBragLog.fulfilled, (state, action: PayloadAction<BragLog>) => {
                state.loading = false;
                state.bragLogs.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add bragLog'
            })
            .addCase(modifyBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyBragLog.fulfilled, (state, action: PayloadAction<BragLog>) => {
                state.loading = false;
                const index = state.bragLogs.findIndex(bragLog => bragLog.id === action.payload.id);
                if (index !== -1) {
                    state.bragLogs[index] = action.payload;
                }
                state.lastFetched = null;
            })
            .addCase(modifyBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update bragLog';
            })
            .addCase(removeBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeBragLog.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.bragLogs = state.bragLogs.filter(bragLog => bragLog.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete bragLog';
            });
    }
});

export const { clearBragLogsError, resetBragLogs } = bragLogsSlice.actions;
export default bragLogsSlice.reducer;
