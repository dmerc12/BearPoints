import { BehaviorType, withHealthAwareRetry, api } from '../../index';

export const createBehaviorType = async (behaviorData: Partial<BehaviorType>, signal?: AbortSignal): Promise<BehaviorType> => {
    return await withHealthAwareRetry(() =>
        api.post<BehaviorType>(`api/behavior-types`, behaviorData, { signal }).then(r => r.data));
};
