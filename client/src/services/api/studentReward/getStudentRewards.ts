import { PaginatedStudentRewards, fetchPaginated } from '../../index';

export const getStudentRewards = async (page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedStudentRewards> => {
    return await fetchPaginated<PaginatedStudentRewards>(
        `api/student-rewards?projection=studentRewardProjection&page=${page}&size=${size}`,
        'studentRewards',
        signal
    );
};
