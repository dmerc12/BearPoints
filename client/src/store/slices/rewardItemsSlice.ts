import { getRewardItems, createRewardItem, updateRewardItem, deleteRewardItem } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedRewardItems, RewardItem } from '../../services/types';
import { RootState } from "../index";

interface RewardItemsState {
    rewardItems: RewardItem[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalRewardItems: number;
    };
    lastFetched: number | null;
}

const initialState: RewardItemsState = {
    rewardItems: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalRewardItems: 0
    },
    lastFetched: null,
};

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

export const fetchRewardItems = createAsyncThunk(
    'rewardItems/fetchRewardItems',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.rewardItems.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                rewardItems: state.rewardItems.rewardItems,
                totalPages: state.rewardItems.pagination.totalPages,
                totalRewardItems: state.rewardItems.pagination.totalRewardItems
            };
        }
        return await getRewardItems(params.page, params.size, signal);
    }
);

export const addRewardItem = createAsyncThunk(
    'rewardItems/addRewardItem',
    async (rewardItemData: Partial<RewardItem>, { signal }) => {
        return await createRewardItem(rewardItemData, signal);
    }
);

export const modifyRewardItem = createAsyncThunk(
    'rewardItems/modifyRewardItem',
    async ({ id, rewardItemData }: { id: number, rewardItemData: Partial<RewardItem> }, { signal }) => {
        return await updateRewardItem(id, rewardItemData, signal);
    }
);

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
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchRewardItems.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRewardItems.fulfilled, (state, action: PayloadAction<PaginatedRewardItems>) => {
                state.loading = false;
                state.rewardItems = action.payload.rewardItems;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalRewardItems: action.payload.totalRewardItems
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchRewardItems.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch rewardItems';
            })
            .addCase(addRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addRewardItem.fulfilled, (state, action: PayloadAction<RewardItem>) => {
                state.loading = false;
                state.rewardItems.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add rewardItem'
            })
            .addCase(modifyRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyRewardItem.fulfilled, (state, action: PayloadAction<RewardItem>) => {
                state.loading = false;
                const index = state.rewardItems.findIndex(rewardItem => rewardItem.id === action.payload.id);
                if (index !== -1) {
                    state.rewardItems[index] = action.payload;
                }
                state.lastFetched = null;
            })
            .addCase(modifyRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update rewardItem';
            })
            .addCase(removeRewardItem.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeRewardItem.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.rewardItems = state.rewardItems.filter(rewardItem => rewardItem.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeRewardItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete rewardItem';
            });
    }
});

export const { clearRewardItemsError, resetRewardItems } = rewardItemsSlice.actions;
export default rewardItemsSlice.reducer;
