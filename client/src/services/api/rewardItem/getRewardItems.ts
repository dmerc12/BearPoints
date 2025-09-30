import { PaginatedRewardItems, fetchPaginated } from '../../index';

export const getRewardItems = async (page = 0, size = 100,
                                     sort?: string, signal?: AbortSignal): Promise<PaginatedRewardItems> => {
    let url = `api/reward-items?projection=rewardItemProjection&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedRewardItems>(
        url,
        'rewardItems',
        signal
    );
};
