import {
    UserDTO, Student, Teacher, BehaviorType,
    BragLogRequest, Timeframe, LeaderboardEntry
} from './types';
import { auth } from '../Auth';
import axios, {AxiosError, AxiosRequestConfig} from 'axios';

// ============== API with base URL ==============
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    withCredentials: true,
    timeout: 10000
});

// ============== HEALTH STATUS TRACKER ==============
let isHealthy = true;
let lastHealthCheck = 0;
const HEALTH_CHECK_INTERVAL = 30000;

api.interceptors.request.use(async (config) => {
    try {
        // Skip health check for health endpoint itself
        if (config.url === '/api/health') return config;
        // Check health if needed
        if (!isHealthy || (Date.now() - lastHealthCheck > HEALTH_CHECK_INTERVAL)) {
            isHealthy = await checkHealth();
            lastHealthCheck = Date.now();
        }
        if (!isHealthy) {
            return Promise.reject(new Error('Backend is unavailable'));
        }
        // Add auth token if user is logged in
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
    console.error('Request configuration error:', error);
    return Promise.reject(error);
});

// Response interceptor for retries
api.interceptors.response.use(undefined, async (error: AxiosError) => {
    if (!error.config) {
        console.error('Request failed without config:', error);
        return Promise.reject(error);
    }
    const config = error.config as AxiosRequestConfig & { _retryCount?: number };
    // Initialize retry count
    config._retryCount = config._retryCount || 0;
    // Only retry on network errors or 5xx status codes
    const isRetryable = !error.response ||
        (error.response.status >= 500 && error.response.status < 600);
    if (isRetryable && config._retryCount < 3) {
        config._retryCount++;
        // Exponential backoff with jitter: base 1s, max 8s
        const delay = Math.min(8000, 1000 * Math.pow(2, config._retryCount));
        const jitter = Math.random() * 500;
        await new Promise (resolve => setTimeout(resolve, delay + jitter));
        return api(config);
    }
    // Update health status on final failure
    if (isRetryable) {
        isHealthy = false;
    }
    return Promise.reject(error);
});

/**
 *  Checks backend health status
 *  @returns Promise resolving to true if backend is healthy
 */
const checkHealth = async (): Promise<boolean> => {
    try {
        const response = await api.get('/health', {
            timeout: 3000,
        });
        // Spring Actuator health response structure
        return response.data?.status === "UP";
    } catch (error) {
        console.error('Health check failed:', error);
        return false;
    }
};

// Wrapper function for API calls with health awareness
const withHealthAwareRetry = async <T>(apiCall: () => Promise<T>): Promise<T> => {
    try {
        return await apiCall();
    } catch (error: unknown) {
        if (error instanceof Error && error.message === 'Backend is unavailable') {
            // Wait for backend to wake up
            await new Promise(resolve => setTimeout(resolve, 2000));
            isHealthy = await checkHealth();
            if (isHealthy) {
                return apiCall();
            }
        }
        if (axios.isAxiosError(error)) {
            console.error('API request failed:', error.response?.status, error.config?.url);
            throw new Error(`API error: ${error.message}`);
        }
        const errorMessage = error instanceof Error ? error.message : String(error);
        throw new Error(`Request failed: ${errorMessage}`);
    }
}

// ============== USER API =================
export const getCurrentUser = async (): Promise<UserDTO> => {
    return withHealthAwareRetry(() =>
        api.get<UserDTO>('/users/me')
            .then(r => r.data));
};

// ============== STUDENT API =================
export const getStudents = async (): Promise<Student[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { students: Student[] } }>('/students')
            .then(r => r.data._embedded.students));
};

export const getStudentByToken = async (token: string): Promise<Student> => {
    return withHealthAwareRetry(() =>
        api.get<Student>(`/students/search/findByToken?token=${token}`)
            .then(r => r.data));
};

// ============== BEHAVIOR API =================
export const getActiveBehaviorTypes = async (): Promise<BehaviorType[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { behaviorTypes: BehaviorType[] } }>('/behavior-types?filter=active')
            .then(r => r.data._embedded.behaviorTypes));
};

// ============== BRAG LOG API =================
export const submitPublicBragLog = async (data: BragLogRequest) => {
    return withHealthAwareRetry(() =>
        api.post('/public/brag-logs', data));
};

// ============== TEACHER API =================
export const getTeachers = async (): Promise<Teacher[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { teachers: Teacher[] } }>('/teachers')
            .then(r => r.data._embedded.teachers));
}

// ============== LEADERBOARD API =================
export const getLeaderboard = async (timeframe: Timeframe): Promise<LeaderboardEntry[]> => {
    return withHealthAwareRetry(() =>
        api.get<LeaderboardEntry[]>(`/leaderboard?timeframe=${timeframe}`)
            .then(r => r.data));
}
