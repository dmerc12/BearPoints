import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { PagedResponseDTO, TeacherDTO } from '../../types';
import { api } from '../api';

export const getTeachers = async (page = 0, size = 20,
                                  sort?: string, signal?: AbortSignal): Promise<PagedResponseDTO<TeacherDTO>> => {
    let url = `api/teachers?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<TeacherDTO>>(url, { signal })
            .then(r => r.data));
};
