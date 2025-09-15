import { checkHealth } from './index';

/**
 * Ensures backend is healthy before making API calls
 * Will retry health checks until backend is sup or max retries is reached
 */
export const ensureBackendHealthy = async (maxRetries = 5, initialDelay = 2000, signal?: AbortSignal): Promise<boolean> => {
    let retries = 0;
    let delay = initialDelay;
    while (retries < maxRetries) {
        const isHealthy = await checkHealth(signal);
        if (isHealthy) return true;
        console.log(`Backend unhealthy. Retrying in ${delay}ms (${retries + 1}/${maxRetries})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        delay *= 2;
        retries++;
    }
    throw new Error(`Backend unavailable after ${maxRetries} retries`);
}
