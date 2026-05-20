import { ensureBackendHealthy } from './index';
import axios from 'axios';

/**
 * Wrapper function that ensures backend health before API calls and retries the call if backend becomes healthy
 * @param apiCall API call to make
 */
export const withHealthAwareRetry = async <T>(apiCall: () => Promise<T>): Promise<T> => {
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
