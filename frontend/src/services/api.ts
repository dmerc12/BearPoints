import { Student, BehaviorLog, BehaviorFormData } from './types';
import { auth } from '../Auth';
import axios from 'axios';

// API with base URL
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
});

api.interceptors.request.use(async (config) => {
    const user = auth.currentUser;
    if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Get students
export const getStudents = async (): Promise<Student[]> => {
    try {
        const response = await api.get<Student[]>('/students');
        return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
        console.error('API Error:', error);
        return [];
    }
};

// Get student by token
export const getStudentByToken = async (token: string): Promise<Student | null> => {
    try {
        const response = await api.get<Student>('/students/token', { params: { token } });
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return null;
    }
};

// Get leaderboard
export const getLeaderboard = async (): Promise<{ behaviorLogs: BehaviorLog[], students: Student[] } > => {
    try {
        const response = await api.get('/leaderboard');
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
        return { behaviorLogs: [], students: []};
    }
};

// Submit behavior report
export const submitBehavior = async (data: BehaviorFormData) => {
    try {
        const response = await api.post('/form/submit', data);
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
    }
};
