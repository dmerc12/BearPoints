import { getUsers, createUser, updateUser, deleteUser } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedUsers, UserDTO } from '../../services/types';
import {RootState} from "../index.ts";

interface UsersState {
    users: UserDTO[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalUsers: number;
    };
    lastFetched: number | null;
}

const initialState: UsersState = {
    users: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalUsers: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchUsers = createAsyncThunk(
    'users/fetchUsers',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.users.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                users: state.users.users,
                totalPages: state.users.pagination.totalPages,
                totalUsers: state.users.pagination.totalUsers
            };
        }
        return await getUsers(params.page, params.size, signal);
    }
);

export const addUser = createAsyncThunk(
    'users/addUser',
    async (userData: Partial<UserDTO>, { signal }) => {
        return await createUser(userData, signal);
    }
);

export const modifyUser = createAsyncThunk(
    'users/modifyUser',
    async ({ id, userData }: { id: number, userData: Partial<UserDTO> }, { signal }) => {
        return await updateUser(id, userData, signal);
    }
);

export const removeUser = createAsyncThunk(
    'users/removeUser',
    async (id: number, { signal }) => {
        await deleteUser(id, signal);
        return id;
    }
);

const usersSlice = createSlice({
   name: 'users',
   initialState,
   reducers: {
       clearUsersError: (state) => {
           state.error = null;
       },
       resetUsers: () => initialState,
   },
   extraReducers: (builder) => {
       builder
           .addCase(fetchUsers.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(fetchUsers.fulfilled, (state, action: PayloadAction<PaginatedUsers>) => {
               state.loading = false;
               state.users = action.payload.users;
               state.pagination = {
                   totalPages: action.payload.totalPages,
                   totalUsers: action.payload.totalUsers
               };
               state.lastFetched = Date.now();
           })
           .addCase(fetchUsers.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to fetch users';
           })
           .addCase(addUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(addUser.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               state.users.push(action.payload);
               state.lastFetched = null;
           })
           .addCase(addUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to add user'
           })
           .addCase(modifyUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(modifyUser.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               const index = state.users.findIndex(user => user.id === action.payload.id);
               if (index !== -1) {
                   state.users[index] = action.payload;
               }
               state.lastFetched = null;
           })
           .addCase(modifyUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to update user';
           })
           .addCase(removeUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(removeUser.fulfilled, (state, action: PayloadAction<number>) => {
               state.loading = false;
               state.users = state.users.filter(user => user.id !== action.payload);
               state.lastFetched = null;
           })
           .addCase(removeUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to delete user';
           });
   }
});

export const { clearUsersError, resetUsers } = usersSlice.actions;
export default usersSlice.reducer;
