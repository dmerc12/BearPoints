import { 
    getBragLogs, createBragLog, updateBragLog, deleteBragLog, submitPublicBragLog,
    BragLogDTO, CacheResponse, BragLogRequest
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface BragLogsState {
    data: BragLogDTO[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalElements: number;
    };
    lastFetched: number | null;
    currentParams: {
        page: number;
        size: number;
        sort?: string;
    } | null;
}

const initialState: BragLogsState = {
    data: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null,
    currentParams: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchBragLogs = createAsyncThunk(
    'bragLogs/fetchBragLogs',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.bragLogs;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                data: state.bragLogs.data,
                totalPages: state.bragLogs.pagination.totalPages,
                totalElements: state.bragLogs.pagination.totalElements
            } as CacheResponse<BragLogDTO>;
        }
        return await getBragLogs(params.page, params.size, params.sort, signal);
    }
);

export const addBragLog = createAsyncThunk(
    'bragLogs/addBragLog',
    async (bragLogData: Partial<BragLogDTO>, { signal }) => {
        return await createBragLog(bragLogData, signal);
    }
);

export const addPublicBragLog = createAsyncThunk(
    'bragLogs/addPublicBragLog',
    async (data: BragLogRequest, { signal }) => {
        return await submitPublicBragLog(data, signal);
    }
);

export const modifyBragLog = createAsyncThunk(
    'bragLogs/modifyBragLog',
    async ({ id, bragLogData }: { id: number, bragLogData: Partial<BragLogDTO> }, { signal }) => {
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
            .addCase(fetchBragLogs.fulfilled, (state, action) => {
                    state.loading = false;
                    if ('bragLogs' in action.payload) {
                        state.data = action.payload.bragLogs;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalBragLogs
                        };
                    } else  {
                        state.data = action.payload.data
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalElements
                        };
                    }
                    state.currentParams = {
                        page: action.meta.arg.page,
                        size: action.meta.arg.size,
                        sort: action.meta.arg.sort
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
            .addCase(addBragLog.fulfilled, (state, action: PayloadAction<BragLogDTO>) => {
                state.loading = false;
                state.data.push(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add bragLog'
            })
            .addCase(addPublicBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addPublicBragLog.fulfilled, (state) => {
                state.loading = false;
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addPublicBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Public submission failed'
            })
            .addCase(modifyBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyBragLog.fulfilled, (state, action: PayloadAction<BragLogDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(bragLog => bragLog.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
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
                state.data = state.data.filter(bragLog => bragLog.id !== action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete bragLog';
            });
    }
});

export const { clearBragLogsError, resetBragLogs } = bragLogsSlice.actions;
export default bragLogsSlice.reducer;
