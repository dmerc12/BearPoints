import { PaginatedBragLogs, fetchPaginated } from '../../index';

export const getBragLogs = async (page = 0, size = 100,
                                  sort?: string, signal?: AbortSignal): Promise<PaginatedBragLogs> => {
    let url = `api/brag-logs?projection=bragLogProjection&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedBragLogs>(
        url,
        'bragLogs',
        signal
    );
};
