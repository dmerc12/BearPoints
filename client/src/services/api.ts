import {
    PaginatedBehaviorTypes, PaginatedStudents, PaginatedTeachers, PaginatedUsers, PaginatedBragLogs, PaginatedRewardItems, PaginatedStudentRewards,
    Student, BragLogRequest, BehaviorType, Teacher, UserDTO, BragLog, RewardItem, StudentReward, LeaderboardEntry, Timeframe
} from './types';
import axios, { AxiosError, AxiosRequestConfig } from 'axios';
import { auth } from '../Auth';

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

const fetchResource = async <T>(url: string): Promise<T> => {
    return withHealthAwareRetry(() => api.get<T>(url).then(r => r.data))
}

const fetchPaginated = async <T>(
    url: string,
    resourceName: keyof T,
): Promise<T> => {
    return withHealthAwareRetry(async () => {
        interface HalResponse {
            _embedded: Record<string, unknown[]>;
            page: { totalPages: number, totalElements: number }
        }
        const response = await api.get<HalResponse>(url);
        const embeddedKey = Object.keys(response.data._embedded).find(
            key => key.toLowerCase().includes(resourceName as string)
        ) || resourceName as string;
        const resources = response.data._embedded[embeddedKey] || [];
        const totalKey = `total${String(resourceName).charAt(0).toUpperCase() + 
            String(resourceName).slice(1)}` as keyof T;
        return {
            [resourceName]: resources,
            totalPages: response.data.page.totalPages,
            [totalKey]: response.data.page.totalElements
        } as unknown as T;
    });
};

// ============== USER API =================
export const getUsers = async (page = 0, size = 100): Promise<PaginatedUsers> => {
    return await fetchPaginated<PaginatedUsers>(
        `api/users?projection=userProjection&page=${page}&size=${size}`,
        'users'
    )    ;
};

export const getCurrentUser = async (): Promise<UserDTO> => {
    return fetchResource('api/users/me');
};

export const createUser = async (userData: Partial<UserDTO>): Promise<UserDTO> => {
    return await withHealthAwareRetry(() =>
        api.post<UserDTO>(`api/users`, userData).then(r => r.data));
};

export const updateUser = async (id: number, userData: Partial<UserDTO>): Promise<UserDTO> => {
    return await withHealthAwareRetry(() =>
        api.patch<UserDTO>(`api/users/${id}`, userData).then(r => r.data));
};

export const deleteUser = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/users/${id}`));
};

// ============== STUDENT API =================
export const getStudents = async (page = 0, size = 100): Promise<PaginatedStudents> => {
    return await fetchPaginated<PaginatedStudents>(
        `api/students?projection=studentProjection&page=${page}&size=${size}`,
        'students'
    );
};

export const getStudentByToken = async (token: string): Promise<Student> => {
    return await fetchResource<Student>(`api/students/search/findByToken?token=${token}&projection=studentProjection`);
};

export const createStudent = async (studentData: Partial<Student>): Promise<Student> => {
    return await withHealthAwareRetry(() =>
        api.post<Student>(`api/students`, studentData).then(r => r.data));
};

export const updateStudent = async (id: number, studentData: Partial<Student>): Promise<Student> => {
    return await withHealthAwareRetry(() =>
        api.patch<Student>(`api/students/${id}`, studentData).then(r => r.data));
};

export const deleteStudent = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/students/${id}`));
};

// ============== BEHAVIOR API =================
export const getBehaviorTypes = async (page = 0, size = 100): Promise<PaginatedBehaviorTypes> => {
    return await fetchPaginated<PaginatedBehaviorTypes>(
        `api/behavior-types?projection=behaviorTypeProjection&page=${page}&size=${size}`,
        'behaviorTypes'
    );
};

export const getActiveBehaviorTypes = async (page = 0, size = 100): Promise<PaginatedBehaviorTypes> => {
    return await fetchPaginated<PaginatedBehaviorTypes>(
        `api/behavior-types/search/findByActiveTrue?projection=behaviorTypeProjection&page=${page}&size=${size}`,
        'behaviorTypes'
    );
};

export const createBehaviorType = async (behaviorData: Partial<BehaviorType>): Promise<BehaviorType> => {
    return await withHealthAwareRetry(() =>
        api.post<BehaviorType>(`api/behavior-types`, behaviorData).then(r => r.data));
};

export const updateBehaviorType = async (id: number, behaviorData: Partial<BehaviorType>): Promise<BehaviorType> => {
    return await withHealthAwareRetry(() =>
        api.patch<BehaviorType>(`api/behavior-types/${id}`, behaviorData).then(r => r.data));
};

export const deleteBehaviorType = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/behavior-types/${id}`));
};

// ============== BRAG LOG API =================
export const getBragLogs = async (page = 0, size = 100): Promise<PaginatedBragLogs> => {
    return await fetchPaginated<PaginatedBragLogs>(
        `api/brag-logs?projection=bragLogProjection&page=${page}&size=${size}`,
        'bragLogs'
    );
};

export const createBragLog = async (bragLogData: Partial<BragLog>): Promise<BragLog> => {
    return await withHealthAwareRetry(() =>
        api.post<BragLog>(`api/brag-logs`, bragLogData).then(r => r.data));
};

export const updateBragLog = async (id: number, bragLogData: Partial<BragLog>): Promise<BragLog> => {
    return await withHealthAwareRetry(() =>
        api.patch<BragLog>(`api/brag-logs/${id}`, bragLogData).then(r => r.data));
};

export const deleteBragLog = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/brag-logs/${id}`));
};

export const submitPublicBragLog = async (data: BragLogRequest) => {
    return withHealthAwareRetry(() =>
        api.post('api/public/brag-logs', data));
};

// ============== TEACHER API =================
export const getTeachers = async (page = 0, size = 100): Promise<PaginatedTeachers> => {
    return await fetchPaginated<PaginatedTeachers>(
        `api/teachers?projection=teacherProjection&page=${page}&size=${size}`,
        'teachers'
    );
}

export const createTeacher = async (teacherData: Partial<Teacher>): Promise<Teacher> => {
    return await withHealthAwareRetry(() =>
        api.post<Teacher>(`api/teachers`, teacherData).then(r => r.data));
};

export const updateTeacher = async (id: number, teacherData: Partial<Teacher>): Promise<Teacher> => {
    return await withHealthAwareRetry(() =>
        api.patch<Teacher>(`api/teachers/${id}`, teacherData).then(r => r.data));
};

export const deleteTeacher = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/teachers/${id}`));
};

// ============== LEADERBOARD API =================
export const getLeaderboard = async (timeframe: Timeframe): Promise<LeaderboardEntry[]> => {
    return await fetchResource<LeaderboardEntry[]>(`api/leaderboard?timeframe=${timeframe}`);
}

// ============== REWARD ITEM API =================
export const getRewardItems = async (page = 0, size = 100): Promise<PaginatedRewardItems> => {
    return await fetchPaginated<PaginatedRewardItems>(
        `api/reward-items?projection=rewardItemProjection&page=${page}&size=${size}`,
        'rewardItems'
    );
};

export const createRewardItem = async (rewardItemData: Partial<RewardItem>): Promise<RewardItem> => {
    return await withHealthAwareRetry(() =>
        api.post<RewardItem>(`api/reward-items`, rewardItemData).then(r => r.data));
};

export const updateRewardItem = async (id: number, rewardItemData: Partial<RewardItem>): Promise<RewardItem> => {
    return await withHealthAwareRetry(() =>
        api.patch<RewardItem>(`api/reward-items/${id}`, rewardItemData).then(r => r.data));
};

export const deleteRewardItem = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/reward-items/${id}`));
};

// ============== STUDENT REWARD API =================
export const getStudentRewards = async (page = 0, size = 100): Promise<PaginatedStudentRewards> => {
    return await fetchPaginated<PaginatedStudentRewards>(
        `api/student-rewards?projection=studentRewardProjection&page=${page}&size=${size}`,
        'studentRewards'
    );
};

export const createStudentReward = async (studentRewardData: Partial<StudentReward>): Promise<StudentReward> => {
    return await withHealthAwareRetry(() =>
        api.post<StudentReward>(`api/student-rewards`, studentRewardData).then(r => r.data));
};

export const updateStudentReward = async (id: number, studentRewardData: Partial<StudentReward>): Promise<StudentReward> => {
    return await withHealthAwareRetry(() =>
        api.patch<StudentReward>(`api/student-rewards/${id}`, studentRewardData).then(r => r.data));
};

export const deleteStudentReward = async (id: number): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/student-rewards/${id}`));
};
