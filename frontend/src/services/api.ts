import { Student, BragLog, StudentToken, BehaviorFormData, StudentsResponse, LeaderboardResponse } from './types';
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

// Get students
export const getStudents = async (): Promise<StudentsResponse> => {
    try {
        const response = await api.get<StudentsResponse>('/students');
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return { students: [], teachers: [] };
    }
};

// Get student by token
export const getStudentByToken = async (token: string): Promise<StudentToken | null> => {
    try {
        const response = await api.get<StudentToken>('/students/token', { params: { token } });
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return null;
    }
};

// Get leaderboard
export const getLeaderboard = async (): Promise<LeaderboardResponse> => {
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

// Submit behavior report
export const submitBehavior = async (data: BehaviorFormData) => {
    try {
        const response = await api.post<{ success: boolean }>('/form/submit', data);
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
};
