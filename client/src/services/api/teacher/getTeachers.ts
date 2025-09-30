import { PaginatedTeachers, fetchPaginated } from '../../index';

export const getTeachers = async (page = 0, size = 100,
                                  sort?: string, signal?: AbortSignal): Promise<PaginatedTeachers> => {
    let url = `api/teachers?projection=teacherProjection&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedTeachers>(
        url,
        'teachers',
        signal
    );
};
