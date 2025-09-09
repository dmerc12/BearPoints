import { 
    getRewardItems, createRewardItem, updateRewardItem, deleteRewardItem,
    PaginatedRewardItems, RewardItem, CacheResponse
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface RewardItemsState {
    data: RewardItem[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalElements: number;
    };
    lastFetched: number | null;
}

const initialState: RewardItemsState = {
    data: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
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
                data: state.rewardItems.data,
                totalPages: state.rewardItems.pagination.totalPages,
                totalElements: state.rewardItems.pagination.totalElements
            } as CacheResponse<RewardItem>;
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
            .addCase(fetchRewardItems.fulfilled, (
                state, 
                action: PayloadAction<PaginatedRewardItems | CacheResponse<RewardItem>>) => {
                    state.loading = false;
                    if ('rewardItems' in action.payload) {
                        state.data = action.payload.rewardItems;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalRewardItems
                        };
                    } else {
                        state.data = action.payload.data;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalElements
                        };
                    }
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
                state.data.push(action.payload);
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
                const index = state.data.findIndex(rewardItem => rewardItem.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
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
                state.data = state.data.filter(rewardItem => rewardItem.id !== action.payload);
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
