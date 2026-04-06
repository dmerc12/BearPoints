import { PaginatedStudentRewards, fetchPaginated } from '../../index';

export const getStudentRewards = async (page = 0, size = 100,
                                        sortQuery?: string, signal?: AbortSignal): Promise<PaginatedStudentRewards> => {
    let url = `api/student-rewards?projection=studentRewardProjection&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&sort=${sortQuery}`;
    }
    return await fetchPaginated<PaginatedStudentRewards>(url,'studentRewards', signal);
};
