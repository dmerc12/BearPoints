import {
    getTeachers, searchTeachers, getTeacherById, createTeacher, updateTeacher, deleteTeacher,
    TeacherDTO, PagedResponseDTO, GradeLevel
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface TeachersState {
    data: TeacherDTO[];
    selectedTeacher: TeacherDTO | null;
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
        grade?: GradeLevel;
    } | null;
}

const initialState: TeachersState = {
    data: [],
    selectedTeacher: null,
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

// Fetch all teachers (unfiltered)
export const fetchTeachers = createAsyncThunk(
    'teachers/fetchTeachers',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.teachers;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.email &&
            !currentParams.firstName &&
            !currentParams.lastName &&
            !currentParams.grade;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.teachers.data,
                totalPages: state.teachers.pagination.totalPages,
                totalElements: state.teachers.pagination.totalElements
            } as PagedResponseDTO<TeacherDTO>;
        }
        return await getTeachers(params.page, params.size, params.sort, signal);
    }
);

// Search teachers with filters
export const searchTeachersInList = createAsyncThunk(
    'teachers/searchTeachersInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        email?: string;
        firstName?: string;
        lastName?: string;
        grade?: GradeLevel;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.teachers;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.email === params.email &&
            currentParams.firstName === params.firstName &&
            currentParams.lastName === params.lastName &&
            currentParams.grade === params.grade;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.teachers.data,
                totalPages: state.teachers.pagination.totalPages,
                totalElements: state.teachers.pagination.totalElements
            } as PagedResponseDTO<TeacherDTO>;
        }
        return await searchTeachers(params, signal);
    }
);

// Fetch single teacher by ID
export const fetchTeacherById = createAsyncThunk(
    'teachers/fetchTeacherById',
    async (id: number, { signal }) => {
        return await getTeacherById(id, signal);
    }
);

// Create a new teacher
export const addTeacher = createAsyncThunk(
    'teachers/addTeacher',
    async (teacherData: TeacherDTO, { signal }) => {
        return await createTeacher(teacherData, signal);
    }
);

// Update an existing teacher
export const modifyTeacher = createAsyncThunk(
    'teachers/modifyTeacher',
    async ({ id, teacherData }: { id: number, teacherData: TeacherDTO }, { signal }) => {
        return await updateTeacher(id, teacherData, signal);
    }
);

// Delete a teacher
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
        clearSelectedTeacher: (state) => {
            state.selectedTeacher = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Fetch teachers
            .addCase(fetchTeachers.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchTeachers.fulfilled, (state, action) => {
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
            .addCase(fetchTeachers.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch teachers';
            })
            // Search teachers
            .addCase(searchTeachersInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchTeachersInList.fulfilled, (state, action) => {
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
                    grade: action.meta.arg.grade
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchTeachersInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search teachers'
            })
            // Fetch teacher by ID
            .addCase(fetchTeacherById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchTeacherById.fulfilled, (state, action: PayloadAction<TeacherDTO>) => {
                state.loading = false;
                state.selectedTeacher = action.payload;
            })
            .addCase(fetchTeacherById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch teacher';
            })
            // Add teacher
            .addCase(addTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addTeacher.fulfilled, (state, action: PayloadAction<TeacherDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add teacher'
            })
            // Modify teacher
            .addCase(modifyTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyTeacher.fulfilled, (state, action: PayloadAction<TeacherDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(teacher => teacher.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                if (state.selectedTeacher?.id === action.payload.id) {
                    state.selectedTeacher = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update teacher';
            })
            // Remove teacher
            .addCase(removeTeacher.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeTeacher.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(teacher => teacher.id !== action.payload);
                if (state.selectedTeacher?.id === action.payload) {
                    state.selectedTeacher = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeTeacher.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete teacher';
            });
    }
});

export const { clearTeachersError, resetTeachers, clearSelectedTeacher } = teachersSlice.actions;
export default teachersSlice.reducer;
