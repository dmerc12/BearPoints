import { PaginatedTeachers, fetchPaginated } from '../../index';

export const getTeachers = async (page = 0, size = 100,
                                  sortQuery?: string, signal?: AbortSignal): Promise<PaginatedTeachers> => {
    let url = `api/teachers?projection=teacherProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedTeachers>(url,'teachers', signal);
};
