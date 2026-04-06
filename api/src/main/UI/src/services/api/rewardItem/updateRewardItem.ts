import { RewardItem, withHealthAwareRetry, api } from '../../index';

export const updateRewardItem = async (id: number, rewardItemData: Partial<RewardItem>, signal?: AbortSignal): Promise<RewardItem> => {
    return await withHealthAwareRetry(() =>
        api.patch<RewardItem>(`api/reward-items/${id}`, rewardItemData, { signal }).then(r => r.data));
};
