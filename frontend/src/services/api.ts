import {
    UserDTO, Student, Teacher,
    BehaviorType, BragLogRequest, Timeframe, LeaderboardEntry
} from './types';
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

// ============== USER API =================
export const getCurrentUser = async (): Promise<UserDTO> => {
    const response = await api
        .get<UserDTO>('/users/me');
    return response.data;
};

// ============== STUDENT API =================
export const getStudents = async (): Promise<Student[]> => {
    const response = await api
        .get<{ _embedded: { students: Student[] } }>('/students');
    return response.data._embedded.students;
};

export const getStudentByToken = async (token: string): Promise<Student> => {
    const response = await api
        .get<Student>(`/students/search/findByToken?token=${token}`);
    return response.data;
};

// ============== BEHAVIOR API =================
export const getActiveBehaviorTypes = async (): Promise<BehaviorType[]> => {
    const response = await api
        .get<{ _embedded: { behaviorTypes: BehaviorType[] } }>('/behavior-types?filter=active');
    return response.data._embedded.behaviorTypes;
};

// ============== BRAG LOG API =================
export const submitPublicBragLog = async (data: BragLogRequest) => {
    return await api.post('/public/brag-logs', data);
};

// ============== TEACHER API =================
export const getTeachers = async (): Promise<Teacher[]> => {
    const response = await api
        .get<{ _embedded: { teachers: Teacher[] } }>('/teachers');
    return response.data._embedded.teachers;
}

// ============== LEADERBOARD API =================
export const getLeaderboard = async (timeframe: Timeframe): Promise<LeaderboardEntry[]> => {
    const response = await api.get<LeaderboardEntry[]>(`/leaderboard?timeframe=${timeframe}`);
    return response.data;
}
