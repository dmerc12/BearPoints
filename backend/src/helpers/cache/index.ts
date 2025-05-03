import { CachedData} from '../../types';
import { fetchData } from '../sheets';

let cache: CachedData | null = null;
const CACHE_TTL_MS = 60000;

export const getCacheTimestamp = () => cache?.timestamp;

export const getCachedSheets = async (forceRefresh = false): Promise<CachedData['data']> => {
    try {
        const now = Date.now();
        // Return cached data if valid and not forced to refresh
        if (cache && !forceRefresh && (now - cache.timestamp) < CACHE_TTL_MS) {
            return cache.data;
        }
        const { studentRows, teacherRows, bragRows } = await fetchData();
        cache = {
            timestamp: now,
            data: { studentRows, teacherRows, bragRows }
        };
        return cache.data;
    } catch (error) {
        if (cache) {
            console.error('Sheets API failed - using stale cache');
            return cache.data;
        }
        throw error;
    }
};

export const invalidateCache = () => {
    cache = null;
};
