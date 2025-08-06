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
let healthCheckInProgress = false;
let lastHealthCheckStatus: boolean | null = null;

api.interceptors.request.use(async (config) => {
    try {
        // Skip health check for health endpoint itself
        if (config.url  && config.url.includes('actuator/health')) return config;
        // Add auth token if user is logged in
        await auth.authStateReady();
        const user = auth.currentUser;
        if (user) {
            const token = await user.getIdToken();
            config.headers.Authorization = `Bearer ${token}`;
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
    config._retryCount = config._retryCount || 0;
    // Only retry on network errors or 5xx status codes
    const isRetryable = !error.response ||
        (error.response.status >= 500 && error.response.status < 600);
    if (isRetryable && config._retryCount < 3) {
        config._retryCount++;
        // Exponential backoff with jitter: base 1s, max 8s
        const delay = Math.min(8000, 1000 * Math.pow(2, config._retryCount));
        const jitter = Math.random() * 500;
        console.log(`Retrying request (attempt ${config._retryCount}) in ${delay + jitter}ms`);
        await new Promise (resolve => setTimeout(resolve, delay + jitter));
        return api(config);
    }
    return Promise.reject(error);
});

/**
 *  Checks backend health status with recursion guard
 *  @returns Promise resolving to true if backend is healthy
 */
const checkHealth = async (): Promise<boolean> => {
    if (healthCheckInProgress) {
        console.log('Health check already in progress. Skipping duplicate check.');
        return lastHealthCheckStatus || false;
    }
    try {
        healthCheckInProgress = true;
        console.log('Starting health check')
        const response = await api.get('actuator/health', {
            timeout: 3000,
        });
        const isHealthy = response.data?.status === 'UP';
        console.log(`Health check completed ${isHealthy ? 'UP' : 'DOWN'}`);
        lastHealthCheckStatus = isHealthy;
        return isHealthy;
    } catch (error) {
        console.error('Health check failed:', error);
        lastHealthCheckStatus = false;
        return false;
    } finally {
        healthCheckInProgress = false;
    }
};

/**
 * Ensures backend is healthy before making API calls
 * Will retry health checks until backend is sup or max retries is reached
 */
const ensureBackendHealthy = async (maxRetries = 5, initialDelay = 2000): Promise<boolean> => {
    let retries = 0;
    let delay = initialDelay;
    while (retries < maxRetries) {
        const isHealthy = await checkHealth();
        if (isHealthy) return true;
        console.log(`Backend unhealthy. Retrying in ${delay}ms (${retries + 1}/${maxRetries})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        delay *= 2;
        retries++;
    }
    throw new Error(`Backend unavailable after ${maxRetries} retries`);
}

/**
 * Wrapper function that ensures backend health before API calls and retries the call if backend becomes healthy
 * @param apiCall API call to make
 */
const withHealthAwareRetry = async <T>(apiCall: () => Promise<T>): Promise<T> => {
    try {
        return await apiCall();
    } catch (error) {
        const isUnavailable = axios.isAxiosError(error) &&
            (!error.response || error.response.status === 503);
        if (!isUnavailable) throw error;
        console.log('Backend unavailable, waiting for recovery...');
        await ensureBackendHealthy();
        console.log('Retrying API call after backend recovery');
        return apiCall();
    }
}

// ============== USER API =================
export const getCurrentUser = async (): Promise<UserDTO> => {
    return withHealthAwareRetry(() =>
        api.get<UserDTO>('api/users/me')
            .then(r => r.data));
};

// ============== STUDENT API =================
export const getStudents = async (): Promise<Student[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { students: Student[] } }>('api/students')
            .then(r => r.data._embedded.students));
};

export const getStudentByToken = async (token: string): Promise<Student> => {
    return withHealthAwareRetry(() =>
        api.get<Student>(`api/students/search/findByToken?token=${token}`)
            .then(r => r.data));
};

// ============== BEHAVIOR API =================
export const getActiveBehaviorTypes = async (): Promise<BehaviorType[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { behaviorTypes: BehaviorType[] } }>('api/behavior-types?filter=active')
            .then(r => r.data._embedded.behaviorTypes));
};

// ============== BRAG LOG API =================
export const submitPublicBragLog = async (data: BragLogRequest) => {
    return withHealthAwareRetry(() =>
        api.post('api/public/brag-logs', data));
};

// ============== TEACHER API =================
export const getTeachers = async (): Promise<Teacher[]> => {
    return withHealthAwareRetry(() =>
        api.get<{ _embedded: { teachers: Teacher[] } }>('api/teachers')
            .then(r => r.data._embedded.teachers));
}

// ============== LEADERBOARD API =================
export const getLeaderboard = async (timeframe: Timeframe): Promise<LeaderboardEntry[]> => {
    return withHealthAwareRetry(() =>
        api.get<LeaderboardEntry[]>(`api/leaderboard?timeframe=${timeframe}`)
            .then(r => r.data));
}
