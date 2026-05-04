import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { PagedResponseDTO, StudentDTO } from '../../types';
import { api } from '../api';

export const searchStudents = async (params: { email?: string, firstName?: string,
    lastName?: string, teacherId?: number, minPoints?:  number, maxPoints?: number, page?: number, size?: number,
    sort?: string }, signal?: AbortSignal): Promise<PagedResponseDTO<StudentDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<StudentDTO>>(`api/students/search?${searchParams}`, { signal })
            .then(r => r.data));
}
