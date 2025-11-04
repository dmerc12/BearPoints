import { withHealthAwareRetry, api } from '../index';

export const deleteBehaviorType = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/behavior-types/${id}`, { signal }));
};
