import { Student, LeaderboardEntry, BehaviorFormData } from './types';
import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL
});

export const getStudents = async (): Promise<Student[]> => {
    try {
        const response = await api.get<Student[]>('/students');
        console.log('Students API response:', response);
        return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
        console.error('API Error:', error);
        return [];
    }
};

export const getLeaderboard = async (params?: { teacher?: string; timeframe?: string; }): Promise<LeaderboardEntry[]> => {
    try {
        const response = await api.get<LeaderboardEntry[]>('/leaderboard', { params });
        return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
        console.error('API Error:', error);
        return [];
    }
};

export const submitBehavior = async (data: BehaviorFormData) => {
    try {
        const response = await api.post('/behaviors', data);
        return response.data;
    } catch (error) {
        console.error('API Error:', error);
    }
};
