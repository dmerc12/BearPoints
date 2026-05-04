import { PagedResponseDTO, TeacherDTO, GradeLevel } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const searchTeachers = async (params: { email?: string, firstName?: string,
    lastName?: string, grade?: GradeLevel, page?: number, size?: number, sort?: string }, signal?: AbortSignal)
    : Promise<PagedResponseDTO<TeacherDTO>> => {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null) searchParams.append(key, String(value));
        });
        return withHealthAwareRetry(() =>
            api.get<PagedResponseDTO<TeacherDTO>>(`api/teachers/search?${searchParams}`, { signal })
                .then(r => r.data));
};
