import { Student, BragLog, StudentToken, BehaviorFormData, StudentsResponse, LeaderboardResponse, HealthResponse } from './types';
import { auth } from '../Auth';
import axios from 'axios';

// API with base URL
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    withCredentials: true
});

api.interceptors.request.use(async (config) => {
    try {
        await auth.authStateReady();
        const user = auth.currentUser;
        if (user) {
            const token = await user.getIdToken();
            config.headers.Authorization = `Bearer ${token}`;
            config.headers['Content-Type'] = 'application/json';
        }
        return config;
    } catch (error) {
        console.error('Token refresh failed:', error);
        throw error;
    }
}, (error) => {
    return Promise.reject(error);
});

// Health check
const checkHealth = async (): Promise<HealthResponse> => {
    try {
        const response = await api.get<HealthResponse>('/health');
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return {
            healthy: false,
            details: {
                spreadsheetId: '',
                sheetTitles: [],
                studentsCount: 0,
                teachersCount: 0,
                bragsCount: 0
            }
        };
    }
};

// Health check wrapper
const withHealthCheck = async <T>(operation: () => Promise<T>, options = { maxRetries: 3, initialDelay: 500 }): Promise<T> => {
    const makeAttempt = async (attempt: number): Promise<T> => {
        try {
            const health = await checkHealth();
            if (!health.healthy) {
                throw new Error(`System unhealthy: ${health.error}`);
            }
            return await operation();
        } catch (error) {
            if (attempt >= options.maxRetries) {
                return Promise.reject(error instanceof Error ? error : new Error(String(error)));
            }
            const delay = options.initialDelay * Math.pow(2, attempt - 1);
            await new Promise(resolve => setTimeout(resolve, delay));
            return makeAttempt(attempt + 1);
        }
    };
    return makeAttempt(1);
};

// Get students
const fetchStudents = async (): Promise<StudentsResponse> => {
    try {
        const response = await api.get<StudentsResponse>('/students');
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return { students: [], teachers: [] };
    }
};

export const getStudents = async(): Promise<StudentsResponse> => {
    return withHealthCheck(() => fetchStudents(), {
        maxRetries: 4, initialDelay: 500
    });
};

// Get student by token
const fetchStudentByToken = async (token: string): Promise<StudentToken | null> => {
    try {
        const response = await api.get<StudentToken>('/students/token', { params: { token } });
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return null;
    }
};

export const getStudentByToken = async (token: string): Promise<StudentToken | null> => {
    return withHealthCheck(() => fetchStudentByToken(token), {
        maxRetries: 4, initialDelay: 500
    });
};

// Get leaderboard
const fetchLeaderboard = async (): Promise<LeaderboardResponse> => {
    try {
        const response = await api.get<{ bragLogs: BragLog[], students: Student[] }>('/leaderboard');
        return {
            bragLogs: response.data.bragLogs,
            students: response.data.students,
        };
    } catch (error) {
        console.error('API Error:', error);
        return { bragLogs: [], students: [] };
    }
};

export const getLeaderboard = async (): Promise<LeaderboardResponse> => {
    return withHealthCheck(() => fetchLeaderboard(), {
        maxRetries: 4, initialDelay: 500
    });
};

// Submit behavior report
const sendBehavior = async (data: BehaviorFormData) => {
    try {
        const response = await api.post<{ success: boolean }>('/form/submit', data);
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
};

export const submitBehavior = async (data: BehaviorFormData) => {
    return withHealthCheck(() => sendBehavior(data), {
        maxRetries: 4, initialDelay: 500
    });
};
