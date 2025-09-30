import { PaginatedStudentRewards, fetchPaginated } from '../../index';

export const getStudentRewards = async (page = 0, size = 100,
                                        sort?: string, signal?: AbortSignal): Promise<PaginatedStudentRewards> => {
    let url = `api/student-rewards?projection=studentRewardProjection&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedStudentRewards>(
        url,
        'studentRewards',
        signal
    );
};
