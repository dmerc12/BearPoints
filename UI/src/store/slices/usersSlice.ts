import {
    getUsers, searchUsers, getUserById, createUser, updateUser, deleteUser,
    UserDTO, Role, PagedResponseDTO
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface UsersState {
    data: UserDTO[];
    selectedUser: UserDTO | null;
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
        email?: string;
        firstName?: string;
        lastName?: string;
        role?: Role;
    } | null;
}

const initialState: UsersState = {
    data: [],
    selectedUser: null,
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null,
    currentParams: null
};

const CACHE_DURATION = 5 * 60 * 1000;

// Fetch all users (unfiltered)
export const fetchUsers = createAsyncThunk(
    'users/fetchUsers',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.users;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.email &&
            !currentParams.firstName &&
            !currentParams.lastName &&
            !currentParams.role;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.users.data,
                totalPages: state.users.pagination.totalPages,
                totalElements: state.users.pagination.totalElements
            } as PagedResponseDTO<UserDTO>;
        }
        return await getUsers(params.page, params.size, params.sort, signal);
    }
);

// Search users with filters
export const searchUsersInList = createAsyncThunk(
    'users/searchUsersInList',
    async (params: {
        page: number,
        size: number,
        sort?: string,
        email?: string,
        firstName?: string,
        lastName?: string,
        role?: Role,
        force?: boolean
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.users;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.email === params.email &&
            currentParams.firstName === params.firstName &&
            currentParams.lastName === params.lastName &&
            currentParams.role === params.role;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.users.data,
                totalPages: state.users.pagination.totalPages,
                totalElements: state.users.pagination.totalElements
            } as PagedResponseDTO<UserDTO>;
        }
        return await searchUsers(params, signal);
    }
);

// Fetch single user by ID
export const fetchUserById = createAsyncThunk(
    'users/fetchUserById',
    async (id: number, { signal }) => {
        return await getUserById(id, signal);
    }
);

// Create a new user (Admin or Staff)
export const addUser = createAsyncThunk(
    'users/addUser',
    async (userData: UserDTO, { signal }) => {
        return await createUser(userData, signal);
    }
);

// Update an existing user
export const modifyUser = createAsyncThunk(
    'users/updateUser',
    async ({ id, userData }: { id: number, userData: UserDTO }, { signal }) => {
        return await updateUser(id, userData, signal);
    }
);

// Delete a user
export const removeUser = createAsyncThunk(
    'users/deleteUser',
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
       clearSelectedUser: (state) => {
           state.selectedUser = null;
       },
   },
   extraReducers: (builder) => {
       builder
           // Fetch users
           .addCase(fetchUsers.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(fetchUsers.fulfilled, (state, action) => {
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
           .addCase(fetchUsers.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to fetch users';
           })
           // Search users
           .addCase(searchUsersInList.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(searchUsersInList.fulfilled, (state, action) => {
               state.loading = false;
               state.data = action.payload.content;
               state.pagination = {
                   totalPages: action.payload.totalPages,
                   totalElements: action.payload.totalElements
               };
               state.currentParams = {
                   page: action.meta.arg.page,
                   size: action.meta.arg.size,
                   sort: action.meta.arg.sort,
                   email: action.meta.arg.email,
                   firstName: action.meta.arg.firstName,
                   lastName: action.meta.arg.lastName,
                   role: action.meta.arg.role
               };
               state.lastFetched = Date.now();
           })
           .addCase(searchUsersInList.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to search users';
           })
           // Fetch user by ID
           .addCase(fetchUserById.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(fetchUserById.fulfilled, (state, action) => {
               state.loading = false;
               state.selectedUser = action.payload;
           })
           .addCase(fetchUserById.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to fetch user'
           })
           // Add user
           .addCase(addUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(addUser.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               state.data.unshift(action.payload);
               state.lastFetched = null;
               state.currentParams = null;
           })
           .addCase(addUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to add user'
           })
           // Modify user
           .addCase(modifyUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(modifyUser.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               const index = state.data.findIndex(user => user.id === action.payload.id);
               if (index !== -1) {
                   state.data[index] = action.payload;
               }
               if (state.selectedUser?.id === action.payload.id) {
                   state.selectedUser = action.payload;
               }
               state.lastFetched = null;
               state.currentParams = null;
           })
           .addCase(modifyUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to update user';
           })
           // Remove user
           .addCase(removeUser.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(removeUser.fulfilled, (state, action: PayloadAction<number>) => {
               state.loading = false;
               state.data = state.data.filter(user => user.id !== action.payload);
               if (state.selectedUser?.id === action.payload) {
                   state.selectedUser = null;
               }
               state.lastFetched = null;
               state.currentParams = null;
           })
           .addCase(removeUser.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to delete user';
           });
   }
});

export const { clearUsersError, resetUsers, clearSelectedUser } = usersSlice.actions;
export default usersSlice.reducer;
