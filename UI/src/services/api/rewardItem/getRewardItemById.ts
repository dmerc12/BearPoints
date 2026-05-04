import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { RewardItemDTO } from '../../types';
import { api } from '../api';

export const getRewardItemById = async (id: number, signal?: AbortSignal)
    : Promise<RewardItemDTO> => {
    return withHealthAwareRetry(() =>
        api.get<RewardItemDTO>(`api/items/${id}`, { signal })
            .then(r => r.data));
};
