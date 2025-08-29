import { getTeachers, createTeacher, updateTeacher, deleteTeacher } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedTeachers, Teacher } from '../../services/types';
import { RootState } from '../index.ts';

interface TeachersState {
    teachers: Teacher[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalTeachers: number;
    };
    lastFetched: number | null;
}

const initialState: TeachersState = {
    teachers: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalTeachers: 0
    },
    lastFetched: null
};

const CACHE_DURATION = 5 * 60 * 1000;

export const fetchTeachers = createAsyncThunk(
    'teachers/fetchTeachers',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.teachers.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                teachers: state.teachers.teachers,
                totalPages: state.teachers.pagination.totalPages,
                totalTeachers: state.teachers.pagination.totalTeachers
            };
        }
        return await getTeachers(params.page, params.size, signal);
    }
);

export const addTeacher = createAsyncThunk(
    'teachers/addTeacher',
    async (teacherData: Partial<Teacher>, { signal }) => {
        return await createTeacher(teacherData, signal);
    }
);

export const modifyTeacher = createAsyncThunk(
    'teachers/modifyTeacher',
    async ({ id, teacherData }: { id: number, teacherData: Partial<Teacher> }, { signal }) => {
        return await updateTeacher(id, teacherData, signal);
    }
);

export const removeTeacher = createAsyncThunk(
    'teachers/removeTeacher',
    async (id: number, { signal }) => {
        await deleteTeacher(id, signal);
        return id;
    }
);

const teachersSlice = createSlice({
    name: 'teachers',
    initialState,
    reducers: {
        clearTeachersError: (state) => {
            state.error = null;
        },
        resetTeachers: () => initialState,
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchTeachers.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchTeachers.fulfilled, (state, action: PayloadAction<PaginatedTeachers>) => {
                state.loading = false;
                state.teachers = action.payload.teachers;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalTeachers: action.payload.totalTeachers
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchTeachers.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch teachers';
            })
            .addCase(addTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addTeacher.fulfilled, (state, action: PayloadAction<Teacher>) => {
                state.loading = false;
                state.teachers.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add teacher'
            })
            .addCase(modifyTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyTeacher.fulfilled, (state, action: PayloadAction<Teacher>) => {
                state.loading = false;
                const index = state.teachers.findIndex(teacher => teacher.id === action.payload.id);
                if (index !== -1) {
                    state.teachers[index] = action.payload;
                }
                state.lastFetched = null;
            })
            .addCase(modifyTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update teacher';
            })
            .addCase(removeTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeTeacher.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.teachers = state.teachers.filter(teacher => teacher.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete teacher';
            });
    }
});

export const { clearTeachersError, resetTeachers } = teachersSlice.actions;
export default teachersSlice.reducer;
