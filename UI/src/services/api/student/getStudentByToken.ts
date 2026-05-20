import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentDTO } from '../../types';
import { api } from '../api';

export const getStudentByToken = async (token: string, signal?: AbortSignal)
    : Promise<StudentDTO> => {
    return withHealthAwareRetry(() =>
        api.get<StudentDTO>(`api/students/token/${token}`, { signal })
            .then(r => r.data));
};
