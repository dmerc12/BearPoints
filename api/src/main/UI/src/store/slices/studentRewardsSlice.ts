import { 
    getStudentRewards, createStudentReward, updateStudentReward, deleteStudentReward,
    StudentRewardDTO, CacheResponse
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {RootState} from '../index';

interface StudentRewardsState {
    data: StudentRewardDTO[];
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

const initialState: StudentRewardsState = {
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

export const fetchStudentRewards = createAsyncThunk(
    'studentRewards/fetchStudentRewards',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.studentRewards;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                data: state.studentRewards.data,
                totalPages: state.studentRewards.pagination.totalPages,
                totalElements: state.studentRewards.pagination.totalElements
            } as CacheResponse<StudentRewardDTO>;
        }
        return await getStudentRewards(params.page, params.size, params.sort, signal);
    }
);

export const addStudentReward = createAsyncThunk(
    'studentRewards/addStudentReward',
    async (studentRewardData: Partial<StudentRewardDTO>, { signal }) => {
        return await createStudentReward(studentRewardData, signal);
    }
);

export const modifyStudentReward = createAsyncThunk(
    'studentRewards/modifyStudentReward',
    async ({ id, studentRewardData }: { id: number, studentRewardData: Partial<StudentRewardDTO> }, { signal }) => {
        return await updateStudentReward(id, studentRewardData, signal);
    }
);

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
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchStudentRewards.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudentRewards.fulfilled, (state, action) => {
                    state.loading = false;
                    if ('studentRewards' in action.payload) {
                        state.data = action.payload.studentRewards;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalStudentRewards
                        };
                    } else {
                        state.data = action.payload.data;
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
            .addCase(fetchStudentRewards.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch studentRewards';
            })
            .addCase(addStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addStudentReward.fulfilled, (state, action: PayloadAction<StudentRewardDTO>) => {
                state.loading = false;
                state.data.push(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add studentReward'
            })
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
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update studentReward';
            })
            .addCase(removeStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeStudentReward.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(studentReward => studentReward.id !== action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete studentReward';
            });
    }
});

export const { clearStudentRewardsError, resetStudentRewards } = studentRewardsSlice.actions;
export default studentRewardsSlice.reducer;
