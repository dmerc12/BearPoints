import {
    getStudents, searchStudents, getStudentById, getStudentByToken, createStudent, updateStudent, deleteStudent,
    StudentDTO, PagedResponseDTO, getClassroomLeaderboard
} from '../../services';
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../index';

interface StudentsState {
    data: StudentDTO[];
    selectedStudent: StudentDTO | null;
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
        teacherId?: number;
        minPoints?: number;
        maxPoints?: number;
    } | null;
}

const initialState: StudentsState = {
    data: [],
    selectedStudent: null,
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

// Fetch all students (unfiltered)
export const fetchStudents = createAsyncThunk(
    'students/fetchStudents',
    async (params: { page: number, size: number, sort?: string, force?: boolean }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.students;
        const isSameParams = currentParams &&
            currentParams.page ===  params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            !currentParams.email &&
            !currentParams.firstName &&
            !currentParams.lastName &&
            !currentParams.teacherId &&
            currentParams.minPoints === undefined &&
            currentParams.maxPoints === undefined;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.students.data,
                totalPages: state.students.pagination.totalPages,
                totalElements: state.students.pagination.totalElements
            } as PagedResponseDTO<StudentDTO>;
        }
        return await getStudents(params.page, params.size, params.sort, signal);
    }
);

// Search students with filters
export const searchStudentsInList = createAsyncThunk(
    'students/searchStudentsInList',
    async (params: {
        page: number;
        size: number;
        sort?: string;
        email?: string;
        firstName?: string;
        lastName?: string;
        teacherId?: number;
        minPoints?: number;
        maxPoints?: number;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const {lastFetched, currentParams} = state.students;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.email === params.email &&
            currentParams.firstName === params.firstName &&
            currentParams.lastName === params.lastName &&
            currentParams.teacherId === params.teacherId &&
            currentParams.minPoints === params.minPoints &&
            currentParams.maxPoints === params.maxPoints;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.students.data,
                totalPages: state.students.pagination.totalPages,
                totalElements: state.students.pagination.totalElements,
            } as PagedResponseDTO<StudentDTO>;
        }
        return await searchStudents(params, signal);
    }
);

// Fetch classroom leaderboard (students by teacher, ordered by points)
export const fetchClassroomLeaderboard = createAsyncThunk(
    'students/fetchClassroomLeaderboard',
    async (params: {
        teacherId: number;
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
    }, { getState, signal }) => {
        const state = getState() as RootState;
        const { lastFetched, currentParams } = state.students;
        const isSameParams = currentParams &&
            currentParams.page === params.page &&
            currentParams.size === params.size &&
            currentParams.sort === params.sort &&
            currentParams.teacherId === params.teacherId;
        const isCacheValid = lastFetched &&
            (Date.now() - lastFetched) < CACHE_DURATION &&
            isSameParams;
        if (isCacheValid && !params.force) {
            return {
                content: state.students.data,
                totalPages: state.students.pagination.totalPages,
                totalElements: state.students.pagination.totalElements,
            } as PagedResponseDTO<StudentDTO>;
        }
        return await getClassroomLeaderboard(params.teacherId, params.page, params.size, params.sort, signal);
    }
);

// Fetch single student by ID
export const fetchStudentById = createAsyncThunk(
    'students/fetchStudentById',
    async (id: number, { signal }) => {
        return await getStudentById(id, signal);
    }
);

// Fetch single student by token
export const fetchStudentByToken = createAsyncThunk(
    'students/fetchStudentByToken',
    async (token: string, { signal }) => {
        return await getStudentByToken(token, signal);
    }
);

// Create a new student
export const addStudent = createAsyncThunk(
    'students/addStudent',
    async (studentData: StudentDTO, { signal }) => {
        return await createStudent(studentData, signal);
    }
);

// Modify an existing student
export const modifyStudent = createAsyncThunk(
    'students/modifyStudent',
    async ({ id, studentData }: { id: number, studentData: StudentDTO }, { signal }) => {
        return await updateStudent(id, studentData, signal);
    }
);

// Delete a student
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
        clearSelectedStudent: (state) => {
            state.selectedStudent = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Fetch students
            .addCase(fetchStudents.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudents.fulfilled, (state, action) => {
                    state.loading = false;
                    state.data = action.payload.content;
                    state.pagination = {
                        totalPages: action.payload.totalPages,
                        totalElements: action.payload.totalElements,
                    };
                    state.currentParams = {
                        page: action.meta.arg.page,
                        size: action.meta.arg.size,
                        sort: action.meta.arg.sort
                    };
                    state.lastFetched = Date.now();
            })
            .addCase(fetchStudents.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch students';
            })
            // Search students
            .addCase(searchStudentsInList.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(searchStudentsInList.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.content;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalElements: action.payload.totalElements,
                };
                state.currentParams = {
                    page: action.meta.arg.page,
                    size: action.meta.arg.size,
                    sort: action.meta.arg.sort,
                    email: action.meta.arg.email,
                    firstName: action.meta.arg.firstName,
                    lastName: action.meta.arg.lastName,
                    teacherId: action.meta.arg.teacherId,
                    minPoints: action.meta.arg.minPoints,
                    maxPoints: action.meta.arg.maxPoints,
                };
                state.lastFetched = Date.now();
            })
            .addCase(searchStudentsInList.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to search students';
            })
            // Fetch classroom leaderboard
            .addCase(fetchClassroomLeaderboard.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchClassroomLeaderboard.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.content;
                state.pagination = {
                    totalPages: action.payload.totalPages,
                    totalElements: action.payload.totalElements,
                };
                state.currentParams = {
                    page: action.meta.arg.page,
                    size: action.meta.arg.size,
                    sort: action.meta.arg.sort,
                    teacherId: action.meta.arg.teacherId
                };
                state.lastFetched = Date.now();
            })
            .addCase(fetchClassroomLeaderboard.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch classroom leaderboard';
            })
            // Fetch student by ID
            .addCase(fetchStudentById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudentById.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedStudent = action.payload;
            })
            .addCase(fetchStudentById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch student';
            })
            // Fetch student by token
            .addCase(fetchStudentByToken.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchStudentByToken.fulfilled, (state, action) => {
                state.loading = false;
                state.selectedStudent = action.payload;
            })
            .addCase(fetchStudentByToken.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to fetch student by token';
            })
            // Add student
            .addCase(addStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addStudent.fulfilled, (state, action: PayloadAction<StudentDTO>) => {
                state.loading = false;
                state.data.unshift(action.payload);
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(addStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to add student'
            })
            // Modify student
            .addCase(modifyStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(modifyStudent.fulfilled, (state, action: PayloadAction<StudentDTO>) => {
                state.loading = false;
                const index = state.data.findIndex(student => student.id === action.payload.id);
                if (index !== -1) {
                    state.data[index] = action.payload;
                }
                if (state.selectedStudent?.id === action.payload.id) {
                    state.selectedStudent = action.payload;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(modifyStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to update student';
            })
            // Remove student
            .addCase(removeStudent.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(removeStudent.fulfilled, (state, action: PayloadAction<number>) => {
                state.loading = false;
                state.data = state.data.filter(student => student.id !== action.payload);
                if (state.selectedStudent?.id === action.payload) {
                    state.selectedStudent = null;
                }
                state.lastFetched = null;
                state.currentParams = null;
            })
            .addCase(removeStudent.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || 'Failed to delete student';
            });
    }
});

export const { clearStudentsError, resetStudents, clearSelectedStudent } = studentsSlice.actions;
export default studentsSlice.reducer;
