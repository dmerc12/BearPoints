import { 
    getBragLogs, searchBragLogs, getBragLogById, createBragLog, updateBragLog, deleteBragLog,
    BragLogDTO, PagedResponseDTO, GradeLevel
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface BragLogsState {
    data: BragLogDTO[];
    selectedBragLog: BragLogDTO | null;
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
        studentName?: string;
        teacherName?: string;
        grade?: GradeLevel;
        minPoints?: number;
        maxPoints?: number;
        startDate?: string;
        endDate?: string;
        teacherId?: number;
        studentId?: number;
        notes?: string;
        submitterName?: string;
        submitterUserId?: number;
    } | null;
}

const initialState: BragLogsState = {
    data: [],
    selectedBragLog: null,
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

// Fetch all brag logs (unfiltered)
export const fetchBragLogs = createAsyncThunk(
    'bragLogs/fetchBragLogs',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.bragLogs;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.studentName &&
            !currentParams.teacherName &&
            !currentParams.grade &&
            currentParams.minPoints === undefined &&
            currentParams.maxPoints === undefined &&
            !currentParams.startDate &&
            !currentParams.endDate &&
            currentParams.teacherId === undefined &&
            currentParams.studentId === undefined &&
            !currentParams.notes &&
            !currentParams.submitterName &&
            currentParams.submitterUserId === undefined;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.bragLogs.data,
                totalPages: state.bragLogs.pagination.totalPages,
                totalElements: state.bragLogs.pagination.totalElements
            } as PagedResponseDTO<BragLogDTO>;
        }
        return await getBragLogs(params.page, params.size, params.sort, signal);
    }
);

// Search brag logs with filters
export const searchBragLogsInList = createAsyncThunk(
    'bragLogs/searchBragLogsInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        studentName?: string;
        teacherName?: string;
        grade?: GradeLevel;
        minPoints?: number;
        maxPoints?: number;
        startDate?: string;
        endDate?: string;
        teacherId?: number;
        studentId?: number;
        notes?: string;
        submitterName?: string;
        submitterUserId?: number;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.bragLogs;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.studentName === params.studentName &&
            currentParams.teacherName === params.teacherName &&
            currentParams.grade === params.grade &&
            currentParams.minPoints === params.minPoints &&
            currentParams.maxPoints === params.maxPoints &&
            currentParams.startDate === params.startDate &&
            currentParams.endDate === params.endDate &&
            currentParams.teacherId === params.teacherId &&
            currentParams.studentId === params.studentId &&
            currentParams.notes === params.notes &&
            currentParams.submitterName === params.submitterName &&
            currentParams.submitterUserId === params.submitterUserId;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.bragLogs.data,
                totalPages: state.bragLogs.pagination.totalPages,
                totalElements: state.bragLogs.pagination.totalElements,
            } as PagedResponseDTO<BragLogDTO>;
        }
        return await searchBragLogs(params, signal);
    }
);

// Fetch single brag log by ID
export const fetchBragLogById = createAsyncThunk(
    'bragLogs/fetchBragLogById',
    async (id: number, { signal }) => {
        return await getBragLogById(id, signal);
    }
);

// Create a new brag log
export const addBragLog = createAsyncThunk(
    'bragLogs/addBragLog',
    async (bragLogData: BragLogDTO, { signal }) => {
        return await createBragLog(bragLogData, signal);
    }
);

// Update an existing brag log
export const modifyBragLog = createAsyncThunk(
    'bragLogs/modifyBragLog',
    async ({ id, bragLogData }: { id: number, bragLogData: BragLogDTO }, { signal }) => {
        return await updateBragLog(id, bragLogData, signal);
    }
);

// Delete a brag log
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
        clearSelectedBragLog: (state) => {
            state.selectedBragLog = null;
        }
    },
    extraReducers: (builder) => {
        builder
            // Fetch brag logs
            .addCase(fetchBragLogs.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBragLogs.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.content;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalElements: action.payload.totalElements
                };
                state.currentParams = {
                    page: action.meta.arg.page,
                    size: action.meta.arg.size,
                    sort: action.meta.arg.sort
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchBragLogs.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch bear brags';
            })
            // Search brag logs
            .addCase(searchBragLogsInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchBragLogsInList.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.content;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalElements: action.payload.totalElements,
                };
                state.currentParams = {
                    page: action.meta.arg.page,
                    size: action.meta.arg.size,
                    sort: action.meta.arg.sort,
                    studentName: action.meta.arg.studentName,
                    teacherName: action.meta.arg.teacherName,
                    grade: action.meta.arg.grade,
                    minPoints: action.meta.arg.minPoints,
                    maxPoints: action.meta.arg.maxPoints,
                    startDate: action.meta.arg.startDate,
                    endDate: action.meta.arg.endDate,
                    teacherId: action.meta.arg.teacherId,
                    studentId: action.meta.arg.studentId,
                    notes: action.meta.arg.notes,
                    submitterName: action.meta.arg.submitterName,
                    submitterUserId: action.meta.arg.submitterUserId,
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchBragLogsInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search bear brags';
            })
            // Fetch brag log by ID
            .addCase(fetchBragLogById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBragLogById.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedBragLog = action.payload;
            })
            .addCase(fetchBragLogById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch bear brag';
            })
            // Add brag log
            .addCase(addBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addBragLog.fulfilled, (state, action: PayloadAction<BragLogDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add bear brag'
            })
            // Modify brag log
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
                if (state.selectedBragLog?.id === action.payload.id) {
                    state.selectedBragLog = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update bear brag';
            })
            // Remove brag log
            .addCase(removeBragLog.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeBragLog.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(bragLog => bragLog.id !== action.payload);
                if (state.selectedBragLog?.id === action.payload) {
                    state.selectedBragLog = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeBragLog.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete bear brag';
            });
    }
});

export const { clearBragLogsError, resetBragLogs, clearSelectedBragLog } = bragLogsSlice.actions;
export default bragLogsSlice.reducer;
