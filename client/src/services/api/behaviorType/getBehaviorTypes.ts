import { PaginatedBehaviorTypes, fetchPaginated } from '../../index';

export const getBehaviorTypes = async (page = 0, size = 100,
                                       sortQuery?: string, signal?: AbortSignal): Promise<PaginatedBehaviorTypes> => {
    let url = `api/behavior-types?projection=behaviorTypeProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedBehaviorTypes>(url,'behaviorTypes', signal);
};
