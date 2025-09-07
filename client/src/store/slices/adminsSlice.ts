import {
    getUsersByRole, createUser, updateUser, deleteUser,
    PaginatedUsers, UserDTO, Role
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface AdminsState {
    data: UserDTO[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalElements: number;
    };
    lastFetched: number | null;
}

const initialState: AdminsState = {
    data: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalElements: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

interface CacheResponse {
    data: UserDTO[];
    totalPages: number;
    totalElements: number;
}

export const fetchAdmins = createAsyncThunk(
    'admins/fetchAdmins',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.admins.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                data: state.admins.data,
                totalPages: state.admins.pagination.totalPages,
                totalElements: state.admins.pagination.totalElements
            } as CacheResponse;
        }
        return await getUsersByRole(Role.ADMIN, params.page, params.size, signal);
    }
);

export const addAdmin = createAsyncThunk(
    'admins/addAdmin',
    async (userData: Partial<UserDTO>, { signal }) => {
        const adminData = {
            ...userData,
            role: Role.ADMIN
        };
        return await createUser(adminData, signal);
    }
);

export const modifyAdmin = createAsyncThunk(
    'admins/modifyAdmin',
    async ({ id, userData }: { id: number, userData: Partial<UserDTO> }, { signal }) => {
        const adminData = {
            ...userData,
            role: Role.ADMIN
        };
        return await updateUser(id, adminData, signal);
    }
);

export const removeAdmin = createAsyncThunk(
    'admins/removeAdmin',
    async (id: number, { signal }) => {
        await deleteUser(id, signal);
        return id;
    }
);

const adminsSlice = createSlice({
   name: 'admins',
   initialState,
   reducers: {
       clearAdminsError: (state) => {
           state.error = null;
       },
       resetAdmins: () => initialState,
   },
   extraReducers: (builder) => {
       builder
           .addCase(fetchAdmins.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(fetchAdmins.fulfilled, (state, action: PayloadAction<PaginatedUsers | CacheResponse>) => {
               state.loading = false;
               if ('users' in action.payload) {
                   state.data = action.payload.users;
                   state.pagination = {
                       totalPages: action.payload.totalPages,
                       totalElements: action.payload.totalUsers
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
           .addCase(fetchAdmins.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to fetch admins';
           })
           .addCase(addAdmin.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(addAdmin.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               state.data.push(action.payload);
               state.lastFetched = null;
           })
           .addCase(addAdmin.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to add admin'
           })
           .addCase(modifyAdmin.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(modifyAdmin.fulfilled, (state, action: PayloadAction<UserDTO>) => {
               state.loading = false;
               const index = state.data.findIndex(admin => admin.id === action.payload.id);
               if (index !== -1) {
                   state.data[index] = action.payload;
               }
               state.lastFetched = null;
           })
           .addCase(modifyAdmin.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to update admin';
           })
           .addCase(removeAdmin.pending, (state) => {
               state.loading = true;
               state.error = null;
           })
           .addCase(removeAdmin.fulfilled, (state, action: PayloadAction<number>) => {
               state.loading = false;
               state.data = state.data.filter(admin => admin.id !== action.payload);
               state.lastFetched = null;
           })
           .addCase(removeAdmin.rejected, (state, action) => {
               state.loading = false;
               state.error = action.error.message || 'Failed to delete admin';
           });
   }
});

export const { clearAdminsError, resetAdmins } = adminsSlice.actions;
export default adminsSlice.reducer;
