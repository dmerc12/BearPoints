import { PaginatedBehaviorTypes, fetchPaginated } from '../../index';

export const getActiveBehaviorTypes = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedBehaviorTypes> => {
    return await fetchPaginated<PaginatedBehaviorTypes>(
        `api/behavior-types/search/findByActiveTrue?projection=behaviorTypeProjection&page=${page}&size=${size}`,
        'behaviorTypes',
        signal
    );
};
