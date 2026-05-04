import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentDTO } from '../../types';
import { api } from '../api';

export const createStudent = async (studentData: StudentDTO, signal?: AbortSignal)
    : Promise<StudentDTO> => {
    return withHealthAwareRetry(() =>
        api.post<StudentDTO>(`api/students`, studentData, { signal })
            .then(r => r.data));
};
