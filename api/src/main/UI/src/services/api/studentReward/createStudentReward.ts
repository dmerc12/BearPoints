import { StudentRewardDTO, withHealthAwareRetry, api } from '../../index';

export const createStudentReward = async (studentRewardData: Partial<StudentRewardDTO>, signal?: AbortSignal): Promise<StudentRewardDTO> => {
    return await withHealthAwareRetry(() =>
        api.post<StudentRewardDTO>(`api/student-rewards`, studentRewardData, { signal }).then(r => r.data));
};
