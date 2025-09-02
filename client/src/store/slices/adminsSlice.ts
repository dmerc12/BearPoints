import { getUsersByRole, createUser, updateUser, deleteUser } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedUsers, UserDTO, Role } from '../../services/types';
import { RootState } from '../index';

interface AdminsState {
    admins: UserDTO[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalAdmins: number;
    };
    lastFetched: number | null;
}

const initialState: AdminsState = {
    admins: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalAdmins: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchAdmins = createAsyncThunk(
    'admins/fetchAdmins',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.admins.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                users: state.admins.admins,
                totalPages: state.admins.pagination.totalPages,
                totalUsers: state.admins.pagination.totalAdmins
            };
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
           .addCase(fetchAdmins.fulfilled, (state, action: PayloadAction<PaginatedUsers>) => {
               state.loading = false;
               state.admins = action.payload.users;
               state.pagination = {
                   totalPages: action.payload.totalPages,
                   totalAdmins: action.payload.totalUsers
               };
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
               state.admins.push(action.payload);
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
               const index = state.admins.findIndex(admin => admin.id === action.payload.id);
               if (index !== -1) {
                   state.admins[index] = action.payload;
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
               state.admins = state.admins.filter(admin => admin.id !== action.payload);
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
