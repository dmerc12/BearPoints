import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentRewardDTO } from '../../types';
import { api } from '../api';

export const getStudentRewardById = async (id: number, signal?: AbortSignal)
    : Promise<StudentRewardDTO> => {
    return withHealthAwareRetry(() =>
        api.get<StudentRewardDTO>(`api/rewards/${id}`, { signal })
            .then(r => r.data));
};
