import { RewardItem, withHealthAwareRetry, api } from '../../index';

export const createRewardItem = async (rewardItemData: Partial<RewardItem>, signal?: AbortSignal): Promise<RewardItem> => {
    return await withHealthAwareRetry(() =>
        api.post<RewardItem>(`api/reward-items`, rewardItemData, { signal }).then(r => r.data));
};
