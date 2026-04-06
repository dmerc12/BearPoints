import { StudentRewardDTO, withHealthAwareRetry, api } from '../../index';

export const updateStudentReward = async (id: number, studentRewardData: Partial<StudentRewardDTO>, signal?: AbortSignal): Promise<StudentRewardDTO> => {
    return await withHealthAwareRetry(() =>
        api.patch<StudentRewardDTO>(`api/student-rewards/${id}`, studentRewardData, { signal }).then(r => r.data));
};
