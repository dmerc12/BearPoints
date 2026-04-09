import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteBehaviorType = async (id: number, signal?: AbortSignal)
    : Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/behaviors/${id}`, { signal }));
};
