import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { PagedResponseDTO, RewardItemDTO } from '../../types';
import { api } from '../api';

export const searchRewardItems = async (params: { name?: string,
    minPointCost?: number, maxPointCost?: number, minStock?: number, maxStock?: number, page?: number,
    size?: number, sort?: string }, signal?: AbortSignal): Promise<PagedResponseDTO<RewardItemDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<RewardItemDTO>>(`api/items/search?${searchParams}`, { signal })
            .then(r => r.data));
};
