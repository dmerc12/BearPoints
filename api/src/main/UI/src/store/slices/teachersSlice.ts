import {
    getTeachers, createTeacher, updateTeacher, deleteTeacher,
    Teacher, CacheResponse
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface TeachersState {
    data: Teacher[];
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
    } | null;
}

const initialState: TeachersState = {
    data: [],
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

export const fetchTeachers = createAsyncThunk(
    'teachers/fetchTeachers',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.teachers;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                data: state.teachers.data,
                totalPages: state.teachers.pagination.totalPages,
                totalElements: state.teachers.pagination.totalElements
            } as CacheResponse<Teacher>;
        }
        return await getTeachers(params.page, params.size, params.sort, signal);
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
            .addCase(fetchTeachers.fulfilled, (state, action) => {
                    state.loading = false;
                    if ('teachers' in action.payload) {
                        state.data = action.payload.teachers;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalTeachers
                        };
                    } else {
                        state.data = action.payload.data;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalElements
                        };
                    }
                    state.currentParams = {
                        page: action.meta.arg.page,
                        size: action.meta.arg.size,
                        sort: action.meta.arg.sort
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
                state.data.push(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
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
                const index = state.data.findIndex(teacher => teacher.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
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
                state.data = state.data.filter(teacher => teacher.id !== action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete teacher';
            });
    }
});

export const { clearTeachersError, resetTeachers } = teachersSlice.actions;
export default teachersSlice.reducer;
