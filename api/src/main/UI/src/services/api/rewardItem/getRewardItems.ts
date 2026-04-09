import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { PagedResponseDTO, RewardItemDTO } from '../../types';
import { api } from '../api';

export const getRewardItems = async (page = 0, size = 20, sort?: string,
                                     signal?: AbortSignal): Promise<PagedResponseDTO<RewardItemDTO>> => {
    let url = `api/items?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<RewardItemDTO>>(url, { signal })
            .then(r => r.data));
};
