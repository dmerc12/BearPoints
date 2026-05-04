import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { RewardItemDTO } from '../../types';
import { api } from '../api';

export const updateRewardItem = async (id: number, rewardItemData: RewardItemDTO,
                                       signal?: AbortSignal): Promise<RewardItemDTO> => {
    return withHealthAwareRetry(() =>
        api.put<RewardItemDTO>(`api/items/${id}`, rewardItemData, { signal })
            .then(r => r.data));
};
