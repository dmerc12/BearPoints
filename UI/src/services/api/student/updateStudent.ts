import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentDTO } from '../../types';
import { api } from '../api';

export const updateStudent = async (id: number, studentData: StudentDTO,
                                    signal?: AbortSignal): Promise<StudentDTO> => {
    return withHealthAwareRetry(() =>
        api.put<StudentDTO>(`api/students/${id}`, studentData, { signal })
            .then(r => r.data));
};
