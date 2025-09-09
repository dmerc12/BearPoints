import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { getLeaderboard, LeaderboardEntry, Timeframe } from '../../services';
import { RootState } from '../index';

interface LeaderboardState {
    entries: LeaderboardEntry[];
    loading: boolean;
    error: string | null;
    currentTimeframe: Timeframe;
    cachedEntries: Record<Timeframe, LeaderboardEntry[]>;
    lastFetched: Record<Timeframe, number | null>;
}

const initialState: LeaderboardState = {
    entries: [],
    loading: false,
    error: null,
    currentTimeframe: Timeframe.WEEK,
    cachedEntries: {
        [Timeframe.WEEK]: [],
        [Timeframe.MONTH]: [],
        [Timeframe.SEMESTER]: [],
        [Timeframe.YEAR]: []
    },
    lastFetched: {
        [Timeframe.WEEK]: null,
        [Timeframe.MONTH]: null,
        [Timeframe.SEMESTER]: null,
        [Timeframe.YEAR]: null
    }
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchLeaderboard = createAsyncThunk(
    'leaderboard/fetchLeaderboard',
    async (params: { timeframe: Timeframe, force?: boolean}, { getState, signal }) => {
        const state = getState() as RootState;
        const { timeframe, force } = params;
        const lastFetched = state.leaderboard.lastFetched[timeframe];
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !force) {
            return { data: state.leaderboard.cachedEntries[timeframe], timeframe };
        }
        const data = await getLeaderboard(timeframe, signal);
        return { data, timeframe }
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
            if (state.cachedEntries[action.payload].length > 0) {
                state.entries = state.cachedEntries[action.payload];
            }
        },
        clearTimeframeCache: (state, action: PayloadAction<Timeframe>) => {
            state.cachedEntries[action.payload] = [];
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
                action: PayloadAction<{ data: LeaderboardEntry[], timeframe: Timeframe}>) => {
                    state.loading = false;
                    const { data, timeframe } = action.payload;
                    state.cachedEntries[timeframe] = data;
                    state.lastFetched[timeframe] = Date.now();
                    if (state.currentTimeframe === timeframe) {
                        state.entries = data;
                    }
            })
            .addCase(fetchLeaderboard.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch leaderboard';
            });
    }
});

export const { clearLeaderboardError, resetLeaderboard, setTimeframe, clearTimeframeCache } = leaderboardSlice.actions;
export default leaderboardSlice.reducer;
