import { getLeaderboard, LeaderboardEntry, Timeframe, CacheResponse } from '../../services';
import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface LeaderboardState {
    data: LeaderboardEntry[];
    loading: boolean;
    error: string | null;
    currentTimeframe: Timeframe;
    pagination: {
        totalPages: number;
        totalElements: number;
    }
    lastFetched: number | null;
    cachedEntries: Record<Timeframe, Record<string, {
        data: LeaderboardEntry[];
        pagination: { totalPages: number; totalElements: number };
        lastFetched: number | null;
    }>>;
}

const initialState: LeaderboardState = {
    data: [],
    loading: false,
    error: null,
    currentTimeframe: Timeframe.WEEK,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null,
    cachedEntries: {
        [Timeframe.WEEK]: {},
        [Timeframe.MONTH]: {},
        [Timeframe.SEMESTER]: {},
        [Timeframe.YEAR]: {}
    },
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchLeaderboard = createAsyncThunk(
    'leaderboard/fetchLeaderboard',
    async (params: { timeframe: Timeframe, page?: number, size?: number, sort?: string, force?: boolean},
           { getState, signal }) => {
        const state = getState() as RootState;
        const { timeframe, page = 0, size = 20, sort, force = false } = params;
        const cacheKey = sort || 'default'
        const cachedTimeframe = state.leaderboard.cachedEntries[timeframe];
        const cachedData = cachedTimeframe ? cachedTimeframe[cacheKey] : null;
        const lastFetched = cachedData?.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !force) {
            return {
                data: cachedData.data,
                totalPages: cachedData.pagination.totalPages,
                totalElements: cachedData.pagination.totalElements
            } as CacheResponse<LeaderboardEntry>;
        }
        return await getLeaderboard(timeframe, page, size, sort, signal);
    }
);

const leaderboardSlice = createSlice({
    name: 'leaderboard',
    initialState,
    reducers: {
        clearLeaderboardError: (state) => {
            state.error = null;
        },
        resetLeaderboard: () => initialState,
        setTimeframe: (state, action: PayloadAction<Timeframe>) => {
            state.currentTimeframe = action.payload;
            state.data = [];
            state.pagination = { totalPages: 0, totalElements: 0 };
        },
        clearTimeframeCache: (state, action: PayloadAction<Timeframe>) => {
            state.cachedEntries[action.payload] = {};
        },
        clearSortCache: (state, action: PayloadAction<{ timeframe: Timeframe, sort?: string }>) => {
            const { timeframe, sort } = action.payload;
            const cacheKey = sort || 'default';
            if (state.cachedEntries[timeframe] && state.cachedEntries[timeframe][cacheKey]) {
                delete state.cachedEntries[timeframe][cacheKey];
            }
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchLeaderboard.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchLeaderboard.fulfilled, (state, action) => {
                    state.loading = false;
                    if ('leaderboardEntries' in action.payload) {
                        state.data = action.payload.leaderboardEntries;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalLeaderboardEntries
                        };
                    } else {
                        state.data = action.payload.data;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalElements
                        };
                    }
                    const timeframe = action.meta.arg.timeframe;
                    const sort = action.meta.arg.sort;
                    const cacheKey = sort || 'default';
                    if (!state.cachedEntries[timeframe]) {
                        state.cachedEntries[timeframe] = {};
                    }
                    state.cachedEntries[timeframe][cacheKey] = {
                        data: state.data,
                        pagination: state.pagination,
                        lastFetched: Date.now()
                    };
                    state.lastFetched = Date.now();
            })
            .addCase(fetchLeaderboard.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch leaderboard';
            });
    }
});

export const { clearLeaderboardError, resetLeaderboard, setTimeframe, clearTimeframeCache, clearSortCache } = leaderboardSlice.actions;
export default leaderboardSlice.reducer;
