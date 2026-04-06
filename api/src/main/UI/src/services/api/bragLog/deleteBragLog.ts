import { withHealthAwareRetry, api } from '../index';

export const deleteBragLog = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/brag-logs/${id}`, { signal }));
};
