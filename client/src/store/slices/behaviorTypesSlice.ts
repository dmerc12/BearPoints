import { getBehaviorTypes, createBehaviorType, updateBehaviorType, deleteBehaviorType } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedBehaviorTypes, BehaviorType } from '../../services/types';
import {RootState} from "../index.ts";

interface BehaviorTypesState {
    behaviorTypes: BehaviorType[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalBehaviorTypes: number;
    };
    lastFetched: number | null;
}

const initialState: BehaviorTypesState = {
    behaviorTypes: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalBehaviorTypes: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchBehaviorTypes = createAsyncThunk(
    'behaviorTypes/fetchBehaviorTypes',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.behaviorTypes.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                behaviorTypes: state.behaviorTypes.behaviorTypes,
                totalPages: state.behaviorTypes.pagination.totalPages,
                totalBehaviorTypes: state.behaviorTypes.pagination.totalBehaviorTypes
            };
        }
        return await getBehaviorTypes(params.page, params.size, signal);
    }
);

export const addBehaviorType = createAsyncThunk(
    'behaviorTypes/addBehaviorType',
    async (behaviorTypeData: Partial<BehaviorType>, { signal }) => {
        return await createBehaviorType(behaviorTypeData, signal);
    }
);

export const modifyBehaviorType = createAsyncThunk(
    'behaviorTypes/modifyBehaviorType',
    async ({ id, behaviorTypeData }: { id: number, behaviorTypeData: Partial<BehaviorType> }, { signal }) => {
        return await updateBehaviorType(id, behaviorTypeData, signal);
    }
);

export const removeBehaviorType = createAsyncThunk(
    'behaviorTypes/removeBehaviorType',
    async (id: number, { signal }) => {
        await deleteBehaviorType(id, signal);
        return id;
    }
);

const behaviorTypesSlice = createSlice({
    name: 'behaviorTypes',
    initialState,
    reducers: {
        clearBehaviorTypesError: (state) => {
            state.error = null;
        },
        resetBehaviorTypes: () => initialState,
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchBehaviorTypes.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBehaviorTypes.fulfilled, (state, action: PayloadAction<PaginatedBehaviorTypes>) => {
                state.loading = false;
                state.behaviorTypes = action.payload.behaviorTypes;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalBehaviorTypes: action.payload.totalBehaviorTypes
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchBehaviorTypes.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch behaviorTypes';
            })
            .addCase(addBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addBehaviorType.fulfilled, (state, action: PayloadAction<BehaviorType>) => {
                state.loading = false;
                state.behaviorTypes.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add behaviorType'
            })
            .addCase(modifyBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyBehaviorType.fulfilled, (state, action: PayloadAction<BehaviorType>) => {
                state.loading = false;
                const index = state.behaviorTypes.findIndex(behaviorType => behaviorType.id === action.payload.id);
                if (index !== -1) {
                    state.behaviorTypes[index] = action.payload;
                }
                state.lastFetched = null;
            })
            .addCase(modifyBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update behaviorType';
            })
            .addCase(removeBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeBehaviorType.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.behaviorTypes = state.behaviorTypes.filter(behaviorType => behaviorType.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete behaviorType';
            });
    }
});

export const { clearBehaviorTypesError, resetBehaviorTypes } = behaviorTypesSlice.actions;
export default behaviorTypesSlice.reducer;
