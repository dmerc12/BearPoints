import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { PagedResponseDTO, BragLogDTO } from '../../types';
import { api } from '../api';

export const getBragLogs = async (page = 0, size = 20, sort?: string,
                                  signal?: AbortSignal): Promise<PagedResponseDTO<BragLogDTO>> => {
    let url = `api/brags?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<BragLogDTO>>(url, { signal })
            .then(r => r.data));
};
