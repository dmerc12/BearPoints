import { BehaviorTypeDTO, withHealthAwareRetry, api } from '../../index';

export const createBehaviorType = async (behaviorData: Partial<BehaviorTypeDTO>, signal?: AbortSignal): Promise<BehaviorTypeDTO> => {
    return await withHealthAwareRetry(() =>
        api.post<BehaviorTypeDTO>(`api/behavior-types`, behaviorData, { signal }).then(r => r.data));
};
