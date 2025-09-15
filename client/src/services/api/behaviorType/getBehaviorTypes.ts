import { PaginatedBehaviorTypes, fetchPaginated } from '../../index';

export const getBehaviorTypes = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedBehaviorTypes> => {
    return await fetchPaginated<PaginatedBehaviorTypes>(
        `api/behavior-types?projection=behaviorTypeProjection&page=${page}&size=${size}`,
        'behaviorTypes',
        signal
    );
};
