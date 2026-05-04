import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { StudentRewardDTO } from '../../types';
import { api } from '../api';

export const createStudentReward = async (studentRewardData: StudentRewardDTO,
                                          signal?: AbortSignal): Promise<StudentRewardDTO> => {
    return withHealthAwareRetry(() =>
        api.post<StudentRewardDTO>(`api/rewards`, studentRewardData, { signal })
            .then(r => r.data));
};
