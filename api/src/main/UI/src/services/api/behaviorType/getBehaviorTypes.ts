import { PagedResponseDTO, BehaviorTypeDTO } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const getBehaviorTypes = async (page = 0, size = 20, sort?: string,
                                       signal?: AbortSignal): Promise<PagedResponseDTO<BehaviorTypeDTO>> => {
    let url = `api/behaviors?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<BehaviorTypeDTO>>(url, { signal })
            .then(r => r.data));
};
