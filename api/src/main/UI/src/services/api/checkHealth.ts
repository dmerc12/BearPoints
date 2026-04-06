import { api } from './index';

// Global health check variables
let healthCheckInProgress = false;
let lastHealthCheckStatus: boolean | null = null;

/**
 *  Checks backend health status with recursion guard
 *  @returns Promise resolving to true if backend is healthy
 */
export const checkHealth = async (signal?: AbortSignal): Promise<boolean> => {
    if (healthCheckInProgress) {
        console.log('Health check already in progress. Skipping duplicate check.');
        return lastHealthCheckStatus || false;
    }
    try {
        healthCheckInProgress = true;
        console.log('Starting health check')
        const response = await api.get('actuator/health', {
            timeout: 3000,
            signal
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
