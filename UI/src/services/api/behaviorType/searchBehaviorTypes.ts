import { PagedResponseDTO, BehaviorTypeDTO } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const searchBehaviorTypes = async (params: { name?: string, active?: boolean,
    minPointValue?: number, maxPointValue?: number, page?: number, size?: number, sort?: string },
                                          signal?: AbortSignal): Promise<PagedResponseDTO<BehaviorTypeDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<BehaviorTypeDTO>>(`api/behaviors/search?${searchParams}`, { signal })
            .then(r => r.data));
};
