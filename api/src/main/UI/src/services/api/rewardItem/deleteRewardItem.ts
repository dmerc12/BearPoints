import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteRewardItem = async (id: number, signal?: AbortSignal)
    : Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/items/${id}`, { signal }));
};
