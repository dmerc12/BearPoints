import { withHealthAwareRetry, api } from '../index';

export const deleteRewardItem = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/reward-items/${id}`, { signal }));
};
