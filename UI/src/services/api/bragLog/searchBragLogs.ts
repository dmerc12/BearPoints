import { PagedResponseDTO, BragLogDTO, GradeLevel } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const searchBragLogs = async (params: { studentName?: string,
    teacherName?: string, grade?: GradeLevel, minPoints?: number, maxPoints?: number, startDate?: string,
    endDate?: string, teacherId?: number, studentId?: number, notes?: string, submitterName?: string,
    submitterUserId?: number, page?: number, size?: number, sort?: string }, signal?: AbortSignal)
    : Promise<PagedResponseDTO<BragLogDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<BragLogDTO>>(`api/brags/search?${searchParams}`, { signal })
            .then(r => r.data));
};
