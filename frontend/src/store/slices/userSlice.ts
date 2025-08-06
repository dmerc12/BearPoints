import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { UserDTO} from '../../services/types.ts';

interface UserState {
    data: UserDTO | null;
    loading: boolean;
    error: string | null;
}

const initialState: UserState = {
    data: null,
    loading: true,
    error: null
};

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {
        setUser: (state, action: PayloadAction<UserDTO>) => {
            state.data = action.payload;
            state.loading = false;
            state.error = null;
        },
        setLoading: (state) => {
            state.loading = true;
            state.error = null;
        },
        setError: (state, action: PayloadAction<string>) => {
            state.data = null;
            state.loading = false;
            state.error = action.payload;
        },
        clearUser: (state) => {
            state.data = null;
            state.loading = false;
            state.error = null;
        }
    }
});

export const { setUser, setLoading, setError, clearUser } = userSlice.actions;
export default userSlice.reducer;
