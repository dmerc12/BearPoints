import { PaginatedBragLogs, fetchPaginated } from '../../index';

export const getBragLogs = async (page = 0, size = 100,
                                  sortQuery?: string, signal?: AbortSignal): Promise<PaginatedBragLogs> => {
    let url = `api/brag-logs?projection=bragLogProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedBragLogs>(url,'bragLogs', signal);
};
