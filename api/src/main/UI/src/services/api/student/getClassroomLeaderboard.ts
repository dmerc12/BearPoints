import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentDTO, PagedResponseDTO } from '../../types';
import { api } from '../api';

export const getClassroomLeaderboard = async (teacherId: number,
                                              page = 0, size = 20, sort?: string, signal?: AbortSignal):
    Promise<PagedResponseDTO<StudentDTO>> => {
    let url = `api/students/leaderboard?teacherId=${teacherId}&page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() => api.get<PagedResponseDTO<StudentDTO>>(url, { signal })
        .then(r => r.data));
}
