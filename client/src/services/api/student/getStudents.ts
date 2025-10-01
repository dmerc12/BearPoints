import { PaginatedStudents, fetchPaginated } from '../../index';

export const getStudents = async (page = 0, size = 100,
                                  sortQuery?: string, signal?: AbortSignal): Promise<PaginatedStudents> => {
    let url = `api/students?projection=studentProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedStudents>(url,'students', signal);
};
