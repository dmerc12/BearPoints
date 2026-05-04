import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentRewardDTO } from '../../types';
import { api } from '../api';

export const updateStudentReward = async (id: number,
                                          studentRewardData: StudentRewardDTO, signal?: AbortSignal)
    : Promise<StudentRewardDTO> => {
    return withHealthAwareRetry(() =>
        api.put<StudentRewardDTO>(`api/rewards/${id}`, studentRewardData, { signal })
            .then(r => r.data));
};
