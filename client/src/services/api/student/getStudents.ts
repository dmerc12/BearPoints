import { PaginatedStudents, fetchPaginated } from '../../index';

export const getStudents = async (page = 0, size = 100,
                                  sort?: string, signal?: AbortSignal): Promise<PaginatedStudents> => {
    let url = `api/students?projection=studentProjection&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedStudents>(
        url,
        'students',
        signal
    );
};
