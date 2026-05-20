import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentDTO } from '../../types';
import { api } from '../api';

export const getStudentById = async (id: number, signal?: AbortSignal)
    : Promise<StudentDTO> => {
    return withHealthAwareRetry(() => api.get<StudentDTO>(`api/students/${id}`, { signal })
        .then(r => r.data));
}