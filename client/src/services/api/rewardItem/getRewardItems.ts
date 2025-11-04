import { PaginatedRewardItems, fetchPaginated } from '../../index';

export const getRewardItems = async (page = 0, size = 100,
                                     sortQuery?: string, signal?: AbortSignal): Promise<PaginatedRewardItems> => {
    let url = `api/reward-items?projection=rewardItemProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedRewardItems>(url,'rewardItems', signal);
};
