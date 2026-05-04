import { PagedResponseDTO, StudentRewardDTO } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const searchStudentRewards = async (params: { studentName?: string,
    studentId?: number, itemName?: string, itemId?: number, minPointsUsed?: number, maxPointsUsed?: number,
    startDate?: string, endDate?: string, page?: number, size?: number, sort?: string }, signal?: AbortSignal)
    : Promise<PagedResponseDTO<StudentRewardDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<StudentRewardDTO>>(`api/rewards/search?${searchParams}`, { signal })
            .then(r => r.data));
};
