import { AxiosError, AxiosRequestConfig } from 'axios';
import { api } from './api';

api.interceptors.response.use(undefined, async (error: AxiosError) => {
    if (!error.config) {
        console.error('Request failed without config:', error);
        return Promise.reject(error);
    }
    if (error.code === 'ERR_CANCELED') {
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
