import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const triggerSync = async (signal?: AbortSignal): Promise<string> => {
    return withHealthAwareRetry(() =>
        api.post<string>('/api/sync', null, {signal})
            .then(r => r.data)
    );
};
