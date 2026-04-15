import {
    getBehaviorTypes, searchBehaviorTypes, getBehaviorTypeById, createBehaviorType, updateBehaviorType, deleteBehaviorType,
    BehaviorTypeDTO, PagedResponseDTO
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface BehaviorTypesState {
    data: BehaviorTypeDTO[];
    selectedBehaviorType: BehaviorTypeDTO | null;
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
        name?: string;
        active?: boolean;
        minPointValue?: number;
        maxPointValue?: number;
    } | null;
}

const initialState: BehaviorTypesState = {
    data: [],
    selectedBehaviorType: null,
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null,
    currentParams: null,
};

const CACHE_DURATION = 5 * 60 * 1000;

// Fetch all behavior types (unfiltered)
export const fetchBehaviorTypes = createAsyncThunk(
    'behaviorTypes/fetchBehaviorTypes',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.behaviorTypes;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.name &&
            currentParams.active === undefined &&
            currentParams.minPointValue === undefined &&
            currentParams.maxPointValue === undefined;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.behaviorTypes.data,
                totalPages: state.behaviorTypes.pagination.totalPages,
                totalElements: state.behaviorTypes.pagination.totalElements
            } as PagedResponseDTO<BehaviorTypeDTO>;
        }
        return await getBehaviorTypes(params.page, params.size, params.sort, signal);
    }
);

// Search behavior types with filters
export const searchBehaviorTypesInList = createAsyncThunk(
    'behaviorTypes/searchBehaviorTypesInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        name?: string;
        active?: boolean;
        minPointValue?: number;
        maxPointValue?: number;
        force?: boolean;
    }, { getState, signal}) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.behaviorTypes;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.name === params.name &&
            currentParams.active === params.active &&
            currentParams.minPointValue === params.minPointValue &&
            currentParams.maxPointValue === params.maxPointValue;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.behaviorTypes.data,
                totalPages: state.behaviorTypes.pagination.totalPages,
                totalElements: state.behaviorTypes.pagination.totalElements,
            } as PagedResponseDTO<BehaviorTypeDTO>;
        }
        return await searchBehaviorTypes(params, signal);
    }
);

// Fetch single behavior type by ID
export const fetchBehaviorTypeById = createAsyncThunk(
    'behaviorTypes/fetchBehaviorTypeById',
    async (id: number, { signal }) => {
        return await getBehaviorTypeById(id, signal);
    }
);

// Create a new behavior type
export const addBehaviorType = createAsyncThunk(
    'behaviorTypes/addBehaviorType',
    async (behaviorTypeData: BehaviorTypeDTO, { signal }) => {
        return await createBehaviorType(behaviorTypeData, signal);
    }
);

// Update an existing behavior type
export const modifyBehaviorType = createAsyncThunk(
    'behaviorTypes/modifyBehaviorType',
    async ({ id, behaviorTypeData }: { id: number, behaviorTypeData: BehaviorTypeDTO }, { signal }) => {
        return await updateBehaviorType(id, behaviorTypeData, signal);
    }
);

// Delete a behavior type
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
        clearSelectedBehaviorType: (state) => {
            state.selectedBehaviorType = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Fetch behavior types
            .addCase(fetchBehaviorTypes.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBehaviorTypes.fulfilled, (state, action) => {
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
                    }
                    state.lastFetched = Date.now();
            })
            .addCase(fetchBehaviorTypes.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch behavior types';
            })
            // Search behavior types
            .addCase(searchBehaviorTypesInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchBehaviorTypesInList.fulfilled, (state, action) => {
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
                    name: action.meta.arg.name,
                    active: action.meta.arg.active,
                    minPointValue: action.meta.arg.minPointValue,
                    maxPointValue: action.meta.arg.maxPointValue,
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchBehaviorTypesInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search behavior types';
            })
            // Fetch behavior type by ID
            .addCase(fetchBehaviorTypeById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBehaviorTypeById.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedBehaviorType = action.payload;
            })
            .addCase(fetchBehaviorTypeById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch behavior type';
            })
            // Add behavior type
            .addCase(addBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addBehaviorType.fulfilled, (state, action: PayloadAction<BehaviorTypeDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add behavior type'
            })
            // Modify behavior type
            .addCase(modifyBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyBehaviorType.fulfilled, (state, action: PayloadAction<BehaviorTypeDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(behaviorType => behaviorType.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                if (state.selectedBehaviorType?.id === action.payload.id) {
                    state.selectedBehaviorType = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update behaviorType';
            })
            // Remove behavior type
            .addCase(removeBehaviorType.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeBehaviorType.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(behaviorType => behaviorType.id !== action.payload);
                if (state.selectedBehaviorType?.id === action.payload) {
                    state.selectedBehaviorType = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeBehaviorType.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete behavior type';
            });
    }
});

export const { clearBehaviorTypesError, resetBehaviorTypes, clearSelectedBehaviorType } = behaviorTypesSlice.actions;
export default behaviorTypesSlice.reducer;
