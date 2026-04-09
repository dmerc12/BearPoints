import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BehaviorTypeDTO } from '../../types';
import { api } from '../api';

export const getBehaviorTypeById = async (id: number, signal?: AbortSignal)
    : Promise<BehaviorTypeDTO> => {
    return withHealthAwareRetry(() =>
        api.get<BehaviorTypeDTO>(`api/behaviors/${id}`, { signal })
            .then(r => r.data));
};
