import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteUser = async (id: number, signal?: AbortSignal): Promise<void> => {
    return withHealthAwareRetry(() => api.delete(`api/users/${id}`, { signal }));
};
