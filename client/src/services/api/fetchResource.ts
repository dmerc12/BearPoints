import { withHealthAwareRetry, api } from './index';

export const fetchResource = async <T>(url: string, signal?: AbortSignal): Promise<T> => {
    return withHealthAwareRetry(() => api.get<T>(url, { signal }).then(r => r.data))
}
