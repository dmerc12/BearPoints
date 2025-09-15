import { BehaviorType, withHealthAwareRetry, api } from '../../index';

export const updateBehaviorType = async (id: number, behaviorData: Partial<BehaviorType>, signal?: AbortSignal): Promise<BehaviorType> => {
    return await withHealthAwareRetry(() =>
        api.patch<BehaviorType>(`api/behavior-types/${id}`, behaviorData, { signal }).then(r => r.data));
};
