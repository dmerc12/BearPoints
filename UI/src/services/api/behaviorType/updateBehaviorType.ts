import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BehaviorTypeDTO } from '../../types';
import { api } from '../api';

export const updateBehaviorType = async (id: number, behaviorData: BehaviorTypeDTO,
                                         signal?: AbortSignal): Promise<BehaviorTypeDTO> => {
    return withHealthAwareRetry(() =>
        api.put<BehaviorTypeDTO>(`api/behaviors/${id}`, behaviorData, { signal })
            .then(r => r.data));
};
