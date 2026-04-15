import { 
    getRewardItems, searchRewardItems, getRewardItemById, createRewardItem, updateRewardItem, deleteRewardItem,
    RewardItemDTO, PagedResponseDTO
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface RewardItemsState {
    data: RewardItemDTO[];
    selectedRewardItem: RewardItemDTO | null;
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
        minPointCost?: number;
        maxPointCost?: number;
        minStock?: number;
        maxStock?: number;
    } | null;
}

const initialState: RewardItemsState = {
    data: [],
    selectedRewardItem: null,
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null,
    currentParams: null
};

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

// Fetch all reward items (unfiltered)
export const fetchRewardItems = createAsyncThunk(
    'rewardItems/fetchRewardItems',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.rewardItems;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.name &&
            currentParams.minPointCost === undefined &&
            currentParams.maxPointCost === undefined &&
            currentParams.minStock === undefined &&
            currentParams.maxStock === undefined;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.rewardItems.data,
                totalPages: state.rewardItems.pagination.totalPages,
                totalElements: state.rewardItems.pagination.totalElements
            } as PagedResponseDTO<RewardItemDTO>;
        }
        return await getRewardItems(params.page, params.size, params.sort, signal);
    }
);

// Search reward items with filter
export const searchRewardItemsInList = createAsyncThunk(
    'rewardItems/searchRewardItemsInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        name?: string;
        minPointCost?: number;
        maxPointCost?: number;
        minStock?: number;
        maxStock?: number;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.rewardItems;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.name === params.name &&
            currentParams.minPointCost === params.minPointCost &&
            currentParams.maxPointCost === params.maxPointCost &&
            currentParams.minStock === params.minStock &&
            currentParams.maxStock === params.maxStock;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.rewardItems.data,
                totalPages: state.rewardItems.pagination.totalPages,
                totalElements: state.rewardItems.pagination.totalElements,
            } as PagedResponseDTO<RewardItemDTO>;
        }
        return await searchRewardItems(params, signal);
    }
);

// Fetch single reward item by ID
export const fetchRewardItemById = createAsyncThunk(
    'rewardItems/fetchRewardItemById',
    async (id: number, { signal }) => {
        return await getRewardItemById(id, signal);
    }
);

// Create a new reward item
export const addRewardItem = createAsyncThunk(
    'rewardItems/addRewardItem',
    async (rewardItemData: RewardItemDTO, { signal }) => {
        return await createRewardItem(rewardItemData, signal);
    }
);

// Update an existing reward item
export const modifyRewardItem = createAsyncThunk(
    'rewardItems/modifyRewardItem',
    async ({ id, rewardItemData }: { id: number, rewardItemData: RewardItemDTO }, { signal }) => {
        return await updateRewardItem(id, rewardItemData, signal);
    }
);

// Delete a reward item
export const removeRewardItem = createAsyncThunk(
    'rewardItems/removeRewardItem',
    async (id: number, { signal }) => {
        await deleteRewardItem(id, signal);
        return id;
    }
);

const rewardItemsSlice = createSlice({
    name: 'rewardItems',
    initialState,
    reducers: {
        clearRewardItemsError: (state) => {
            state.error = null;
        },
        resetRewardItems: () => initialState,
        clearSelectedRewardItem: (state) => {
            state.selectedRewardItem = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Fetch reward items
            .addCase(fetchRewardItems.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRewardItems.fulfilled, (state, action) => {
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
            .addCase(fetchRewardItems.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch reward items';
            })
            // Search reward items
            .addCase(searchRewardItemsInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchRewardItemsInList.fulfilled, (state, action) => {
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
                    minPointCost: action.meta.arg.minPointCost,
                    maxPointCost: action.meta.arg.maxPointCost,
                    minStock: action.meta.arg.minStock,
                    maxStock: action.meta.arg.maxStock,
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchRewardItemsInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search reward items';
            })
            // Fetch reward item by ID
            .addCase(fetchRewardItemById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRewardItemById.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedRewardItem = action.payload;
            })
            .addCase(fetchRewardItemById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch reward item';
            })
            // Add reward item
            .addCase(addRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addRewardItem.fulfilled, (state, action: PayloadAction<RewardItemDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add reward item'
            })
            // Modify reward item
            .addCase(modifyRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyRewardItem.fulfilled, (state, action: PayloadAction<RewardItemDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(rewardItem => rewardItem.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                if (state.selectedRewardItem?.id === action.payload.id) {
                    state.selectedRewardItem = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update reward item';
            })
            // Remove reward item
            .addCase(removeRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeRewardItem.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(rewardItem => rewardItem.id !== action.payload);
                if (state.selectedRewardItem?.id === action.payload) {
                    state.selectedRewardItem = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete reward item';
            });
    }
});

export const { clearRewardItemsError, resetRewardItems, clearSelectedRewardItem } = rewardItemsSlice.actions;
export default rewardItemsSlice.reducer;
