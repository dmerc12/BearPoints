import { auth } from '../../Auth';
import { api } from './index'

api.interceptors.request.use(async (config) => {
    try {
        // Skip health check for health endpoint itself
        if (config.url  && config.url.includes('/actuator/health')) return config;
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
