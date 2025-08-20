import { getStudentRewards, createStudentReward, updateStudentReward, deleteStudentReward } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedStudentRewards, StudentReward } from '../../services/types';
import {RootState} from "../index.ts";

interface StudentRewardsState {
    studentRewards: StudentReward[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalStudentRewards: number;
    };
    lastFetched: number | null;
}

const initialState: StudentRewardsState = {
    studentRewards: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalStudentRewards: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchStudentRewards = createAsyncThunk(
    'studentRewards/fetchStudentRewards',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.studentRewards.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                studentRewards: state.studentRewards.studentRewards,
                totalPages: state.studentRewards.pagination.totalPages,
                totalStudentRewards: state.studentRewards.pagination.totalStudentRewards
            };
        }
        return await getStudentRewards(params.page, params.size, signal);
    }
);

export const addStudentReward = createAsyncThunk(
    'studentRewards/addStudentReward',
    async (studentRewardData: Partial<StudentReward>, { signal }) => {
        return await createStudentReward(studentRewardData, signal);
    }
);

export const modifyStudentReward = createAsyncThunk(
    'studentRewards/modifyStudentReward',
    async ({ id, studentRewardData }: { id: number, studentRewardData: Partial<StudentReward> }, { signal }) => {
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
            .addCase(fetchStudentRewards.fulfilled, (state, action: PayloadAction<PaginatedStudentRewards>) => {
                state.loading = false;
                state.studentRewards = action.payload.studentRewards;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalStudentRewards: action.payload.totalStudentRewards
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
            .addCase(addStudentReward.fulfilled, (state, action: PayloadAction<StudentReward>) => {
                state.loading = false;
                state.studentRewards.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add studentReward'
            })
            .addCase(modifyStudentReward.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyStudentReward.fulfilled, (state, action: PayloadAction<StudentReward>) => {
                state.loading = false;
                const index = state.studentRewards.findIndex(studentReward => studentReward.id === action.payload.id);
                if (index !== -1) {
                    state.studentRewards[index] = action.payload;
                }
                state.lastFetched = null;
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
                state.studentRewards = state.studentRewards.filter(studentReward => studentReward.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeStudentReward.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete studentReward';
            });
    }
});

export const { clearStudentRewardsError, resetStudentRewards } = studentRewardsSlice.actions;
export default studentRewardsSlice.reducer;
