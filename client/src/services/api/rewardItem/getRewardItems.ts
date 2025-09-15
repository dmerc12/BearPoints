import { PaginatedRewardItems, fetchPaginated } from '../../index';

export const getRewardItems = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedRewardItems> => {
    return await fetchPaginated<PaginatedRewardItems>(
        `api/reward-items?projection=rewardItemProjection&page=${page}&size=${size}`,
        'rewardItems',
        signal
    );
};
