import { PaginatedStudents, fetchPaginated } from '../../index';

export const getStudents = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedStudents> => {
    return await fetchPaginated<PaginatedStudents>(
        `api/students?projection=studentProjection&page=${page}&size=${size}`,
        'students',
        signal
    );
};
