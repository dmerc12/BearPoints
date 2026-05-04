import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteBragLog = async (id: number, signal?: AbortSignal)
    : Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/brags/${id}`, { signal }));
};
