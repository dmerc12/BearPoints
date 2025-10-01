import { PaginatedBehaviorTypes, fetchPaginated } from '../../index';

export const getActiveBehaviorTypes = async (page = 0, size = 100,
                                             sortQuery?: string, signal?: AbortSignal): Promise<PaginatedBehaviorTypes> => {
    let url = `api/behavior-types/search/findByActiveTrue?projection=behaviorTypeProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedBehaviorTypes>(url,'behaviorTypes', signal);
};
