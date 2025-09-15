import { PaginatedBragLogs, fetchPaginated } from '../../index';

export const getBragLogs = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedBragLogs> => {
    return await fetchPaginated<PaginatedBragLogs>(
        `api/brag-logs?projection=bragLogProjection&page=${page}&size=${size}`,
        'bragLogs',
        signal
    );
};
