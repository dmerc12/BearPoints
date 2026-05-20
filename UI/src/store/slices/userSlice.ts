import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { getCurrentUser, UserDTO } from '../../services';
import { RootState } from '../index';

interface UserState {
    data: UserDTO | null;
    loading: boolean;
    error: string | null;
    lastFetched: number | null;
}

const initialState: UserState = {
    data: null,
    loading: true,
    error: null,
    lastFetched: null,
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchCurrentUser = createAsyncThunk(
    'user/fetchCurrentUser',
    async (params: { force?: boolean } = {}, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.user.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (state.user.data && isCacheValid && !params.force) {
            return state.user.data;
        }
        return await getCurrentUser(signal);
    }
);

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {
        clearCurrentUser: (state) => {
            state.data = null;
            state.loading = false;
            state.error = null;
            state.lastFetched = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchCurrentUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchCurrentUser.fulfilled, (state, action: PayloadAction<UserDTO>) => {
                state.loading = false;
                state.data = action.payload;
                state.error = null;
                state.lastFetched = Date.now();
            })
            .addCase(fetchCurrentUser.rejected, (state, action) => {
                state.loading = false;
                if (action.error.name !== 'AbortError') {
                    state.error = action.error.message || 'Failed to fetch user';
                }
            });
    }
});

export const { clearCurrentUser } = userSlice.actions;
export default userSlice.reducer;
