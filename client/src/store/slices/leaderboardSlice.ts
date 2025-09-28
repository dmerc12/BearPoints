import { getLeaderboard, LeaderboardEntry, PaginatedLeaderboardEntries, Timeframe, CacheResponse } from '../../services';
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
    cachedEntries: Record<Timeframe, {
        data: LeaderboardEntry[];
        pagination: { totalPages: number; totalElements: number };
        lastFetched: number | null;
    }>;
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
        [Timeframe.WEEK]: { data: [], pagination: { totalPages: 0, totalElements: 0 }, lastFetched: null },
        [Timeframe.MONTH]: { data: [], pagination: { totalPages: 0, totalElements: 0 }, lastFetched: null },
        [Timeframe.SEMESTER]: { data: [], pagination: { totalPages: 0, totalElements: 0 }, lastFetched: null },
        [Timeframe.YEAR]: { data: [], pagination: { totalPages: 0, totalElements: 0 }, lastFetched: null }
    },
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchLeaderboard = createAsyncThunk(
    'leaderboard/fetchLeaderboard',
    async (params: { timeframe: Timeframe, page?: number, size?: number, force?: boolean},
           { getState, signal }) => {
        const state = getState() as RootState;
        const { timeframe, page = 0, size = 20, force = false } = params;
        const cachedTimeframe = state.leaderboard.cachedEntries[timeframe];
        const lastFetched = cachedTimeframe?.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !force) {
            return {
                data: cachedTimeframe.data,
                totalPages: cachedTimeframe.pagination.totalPages,
                totalElements: cachedTimeframe.pagination.totalElements
            } as CacheResponse<LeaderboardEntry>;
        }
        return await getLeaderboard(timeframe, page, size, signal);
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
            const cachedData = state.cachedEntries[action.payload]
            if (cachedData && cachedData.data.length > 0) {
                state.data = cachedData.data;
                state.pagination = cachedData.pagination;
            } else {
                state.data = [];
                state.pagination = { totalPages: 0, totalElements: 0 };
            }
        },
        clearTimeframeCache: (state, action: PayloadAction<Timeframe>) => {
            state.cachedEntries[action.payload] = {
                data: [],
                pagination: { totalPages: 0, totalElements: 0 },
                lastFetched: null
            };
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchLeaderboard.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchLeaderboard.fulfilled, (
                state,
                action: PayloadAction<PaginatedLeaderboardEntries | CacheResponse<LeaderboardEntry>>) => {
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
                    state.cachedEntries[state.currentTimeframe] = {
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

export const { clearLeaderboardError, resetLeaderboard, setTimeframe, clearTimeframeCache } = leaderboardSlice.actions;
export default leaderboardSlice.reducer;
