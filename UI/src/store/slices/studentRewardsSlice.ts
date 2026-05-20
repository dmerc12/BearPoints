import { 
    getStudentRewards, searchStudentRewards, getStudentRewardById, createStudentReward, updateStudentReward,
    deleteStudentReward, StudentRewardDTO, PagedResponseDTO
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface StudentRewardsState {
    data: StudentRewardDTO[];
    selectedStudentReward: StudentRewardDTO | null;
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
        studentId?: number;
        itemName?: string;
        itemId?: number;
        minPointsUsed?: number;
        maxPointsUsed?: number;
        startDate?: string;
        endDate?: string;
    } | null;
}

const initialState: StudentRewardsState = {
    data: [],
    selectedStudentReward: null,
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

// Fetch all student rewards (unfiltered)
export const fetchStudentRewards = createAsyncThunk(
    'studentRewards/fetchStudentRewards',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.studentRewards;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.studentName &&
            currentParams.studentId === undefined &&
            !currentParams.itemName &&
            currentParams.itemId === undefined &&
            currentParams.minPointsUsed === undefined &&
            currentParams.maxPointsUsed === undefined &&
            !currentParams.startDate &&
            !currentParams.endDate;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.studentRewards.data,
                totalPages: state.studentRewards.pagination.totalPages,
                totalElements: state.studentRewards.pagination.totalElements
            } as PagedResponseDTO<StudentRewardDTO>;
        }
        return await getStudentRewards(params.page, params.size, params.sort, signal);
    }
);

// Search student rewards with filters
export const searchStudentRewardsInList = createAsyncThunk(
    'studentRewards/searchStudentRewardsInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        studentName?: string;
        studentId?: number;
        itemName?: string;
        itemId?: number;
        minPointsUsed?: number;
        maxPointsUsed?: number;
        startDate?: string;
        endDate?: string;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.studentRewards;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.studentName === params.studentName &&
            currentParams.studentId === params.studentId &&
            currentParams.itemName === params.itemName &&
            currentParams.itemId === params.itemId &&
            currentParams.minPointsUsed === params.minPointsUsed &&
            currentParams.maxPointsUsed === params.maxPointsUsed &&
            currentParams.startDate === params.startDate &&
            currentParams.endDate === params.endDate;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.studentRewards.data,
                totalPages: state.studentRewards.pagination.totalPages,
                totalElements: state.studentRewards.pagination.totalElements,
            } as PagedResponseDTO<StudentRewardDTO>;
        }
        return await searchStudentRewards(params, signal);
    }
);

// Fetch single student reward by ID
export const fetchStudentRewardById = createAsyncThunk(
    'studentRewards/fetchStudentRewardById',
    async (id: number, { signal }) => {
        return await getStudentRewardById(id, signal);
    }
);

// Create a new student reward
export const addStudentReward = createAsyncThunk(
    'studentRewards/addStudentReward',
    async (studentRewardData: StudentRewardDTO, { signal }) => {
        return await createStudentReward(studentRewardData, signal);
    }
);

// Update an existing student reward
export const modifyStudentReward = createAsyncThunk(
    'studentRewards/modifyStudentReward',
    async ({ id, studentRewardData }: { id: number, studentRewardData: StudentRewardDTO }, { signal }) => {
        return await updateStudentReward(id, studentRewardData, signal);
    }
);

// Delete a student reward
export const removeStudentReward = createAsyncThunk(
    'studentRewards/removeStudentReward',
    async (id: number, { signal }) => {
        await deleteStudentReward(id, signal);
        return id;
    }
);

const studentRewardsSlice = createSlice({
    name: 'studentRewards',
    initialState,
    reducers: {
        clearStudentRewardsError: (state) => {
            state.error = null;
        },
        resetStudentRewards: () => initialState,
        clearSelectedStudentReward: (state) => {
            state.selectedStudentReward = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Fetch student rewards
            .addCase(fetchStudentRewards.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudentRewards.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.content;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalElements: action.payload.totalElements,
                };
                state.currentParams = {
                    page: action.meta.arg.page,
                    size: action.meta.arg.size,
                    sort: action.meta.arg.sort
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchStudentRewards.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch student rewards';
            })
            // Search student rewards
            .addCase(searchStudentRewardsInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchStudentRewardsInList.fulfilled, (state, action) => {
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
                    studentId: action.meta.arg.studentId,
                    itemName: action.meta.arg.itemName,
                    itemId: action.meta.arg.itemId,
                    minPointsUsed: action.meta.arg.minPointsUsed,
                    maxPointsUsed: action.meta.arg.maxPointsUsed,
                    startDate: action.meta.arg.startDate,
                    endDate: action.meta.arg.endDate,
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchStudentRewardsInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search student rewards';
            })
            // Fetch student reward by ID
            .addCase(fetchStudentRewardById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudentRewardById.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedStudentReward = action.payload;
            })
            .addCase(fetchStudentRewardById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch student reward';
            })
            // Add student reward
            .addCase(addStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addStudentReward.fulfilled, (state, action: PayloadAction<StudentRewardDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add student reward'
            })
            // Modify student reward
            .addCase(modifyStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyStudentReward.fulfilled, (state, action: PayloadAction<StudentRewardDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(studentReward => studentReward.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                if (state.selectedStudentReward?.id === action.payload.id) {
                    state.selectedStudentReward = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update student reward';
            })
            // Remove student reward
            .addCase(removeStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeStudentReward.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(studentReward => studentReward.id !== action.payload);
                if (state.selectedStudentReward?.id === action.payload) {
                    state.selectedStudentReward = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete student reward';
            });
    }
});

export const { clearStudentRewardsError, resetStudentRewards, clearSelectedStudentReward } = studentRewardsSlice.actions;
export default studentRewardsSlice.reducer;
