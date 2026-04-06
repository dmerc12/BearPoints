import { BehaviorTypeDTO, withHealthAwareRetry, api } from '../../index';

export const updateBehaviorType = async (id: number, behaviorData: Partial<BehaviorTypeDTO>, signal?: AbortSignal): Promise<BehaviorTypeDTO> => {
    return await withHealthAwareRetry(() =>
        api.patch<BehaviorTypeDTO>(`api/behavior-types/${id}`, behaviorData, { signal }).then(r => r.data));
};
