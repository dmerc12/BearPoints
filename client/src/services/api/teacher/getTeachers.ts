import { PaginatedTeachers, fetchPaginated } from '../../index';

export const getTeachers = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedTeachers> => {
    return await fetchPaginated<PaginatedTeachers>(
        `api/teachers?projection=teacherProjection&page=${page}&size=${size}`,
        'teachers',
        signal
    );
};
