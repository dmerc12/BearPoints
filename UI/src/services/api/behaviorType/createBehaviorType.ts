import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BehaviorTypeDTO } from '../../types';
import { api } from '../api';

export const createBehaviorType = async (behaviorData: BehaviorTypeDTO,
                                         signal?: AbortSignal): Promise<BehaviorTypeDTO> => {
    return withHealthAwareRetry(() =>
        api.post<BehaviorTypeDTO>(`api/behaviors`, behaviorData, { signal })
            .then(r => r.data));
};
