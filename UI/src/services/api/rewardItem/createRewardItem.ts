import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { RewardItemDTO } from '../../types';
import { api } from '../api';

export const createRewardItem = async (rewardItemData: RewardItemDTO,
                                       signal?: AbortSignal): Promise<RewardItemDTO> => {
    return withHealthAwareRetry(() =>
        api.post<RewardItemDTO>(`api/items`, rewardItemData, { signal })
            .then(r => r.data));
};
