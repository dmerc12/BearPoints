import { 
    getStudents, createStudent, updateStudent, deleteStudent,
    PaginatedStudents, Student, CacheResponse
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {RootState} from '../index';

interface StudentsState {
    data: Student[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalElements: number;
    };
    lastFetched: number | null;
}

const initialState: StudentsState = {
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

export const fetchStudents = createAsyncThunk(
    'students/fetchStudents',
    async (params: { page: number, size: number, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const lastFetched = state.students.lastFetched;
        const isCacheValid = lastFetched && (Date.now() - lastFetched) < CACHE_DURATION;
        if (isCacheValid && !params.force) {
            return {
                data: state.students.data,
                totalPages: state.students.pagination.totalPages,
                totalElements: state.students.pagination.totalElements
            } as CacheResponse<Student>;
        }
        return await getStudents(params.page, params.size, signal);
    }
);

export const addStudent = createAsyncThunk(
    'students/addStudent',
    async (studentData: Partial<Student>, { signal }) => {
        return await createStudent(studentData, signal);
    }
);

export const modifyStudent = createAsyncThunk(
    'students/modifyStudent',
    async ({ id, studentData }: { id: number, studentData: Partial<Student> }, { signal }) => {
        return await updateStudent(id, studentData, signal);
    }
);

export const removeStudent = createAsyncThunk(
    'students/removeStudent',
    async (id: number, { signal }) => {
        await deleteStudent(id, signal);
        return id;
    }
);

const studentsSlice = createSlice({
    name: 'students',
    initialState,
    reducers: {
        clearStudentsError: (state) => {
            state.error = null;
        },
        resetStudents: () => initialState,
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchStudents.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudents.fulfilled, (
                state,
                action: PayloadAction<PaginatedStudents | CacheResponse<Student>>) => {
                    state.loading = false;
                    if ('students' in action.payload) {
                        state.data = action.payload.students;
                        state.pagination = {
                            totalPages: action.payload.totalPages,
                            totalElements: action.payload.totalStudents
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
            .addCase(fetchStudents.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch students';
            })
            .addCase(addStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addStudent.fulfilled, (state, action: PayloadAction<Student>) => {
                state.loading = false;
                state.data.push(action.payload);
                state.lastFetched = null;
            })
            .addCase(addStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add student'
            })
            .addCase(modifyStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyStudent.fulfilled, (state, action: PayloadAction<Student>) => {
                state.loading = false;
                const index = state.data.findIndex(student => student.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                state.lastFetched = null;
            })
            .addCase(modifyStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update student';
            })
            .addCase(removeStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeStudent.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(student => student.id !== action.payload);
                state.lastFetched = null;
            })
            .addCase(removeStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete student';
            });
    }
});

export const { clearStudentsError, resetStudents } = studentsSlice.actions;
export default studentsSlice.reducer;
