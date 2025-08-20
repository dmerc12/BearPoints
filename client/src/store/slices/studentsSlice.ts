import { getStudents, createStudent, updateStudent, deleteStudent } from '../../services/api';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PaginatedStudents, Student } from '../../services/types';
import {RootState} from "../index.ts";

interface StudentsState {
    students: Student[];
    loading: boolean;
    error: string | null;
    pagination: {
        totalPages: number;
        totalStudents: number;
    };
    lastFetched: number | null;
}

const initialState: StudentsState = {
    students: [],
    loading: false,
    error: null,
    pagination: {
        totalPages: 0,
        totalStudents: 0
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
                students: state.students.students,
                totalPages: state.students.pagination.totalPages,
                totalStudents: state.students.pagination.totalStudents
            };
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
            .addCase(fetchStudents.fulfilled, (state, action: PayloadAction<PaginatedStudents>) => {
                state.loading = false;
                state.students = action.payload.students;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalStudents: action.payload.totalStudents
                };
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
                state.students.push(action.payload);
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
                const index = state.students.findIndex(student => student.id === action.payload.id);
                if (index !== -1) {
                    state.students[index] = action.payload;
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
                state.students = state.students.filter(student => student.id !== action.payload);
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
