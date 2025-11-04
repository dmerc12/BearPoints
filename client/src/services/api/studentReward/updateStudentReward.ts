import { StudentReward, withHealthAwareRetry, api } from '../../index';

export const updateStudentReward = async (id: number, studentRewardData: Partial<StudentReward>, signal?: AbortSignal): Promise<StudentReward> => {
    return await withHealthAwareRetry(() =>
        api.patch<StudentReward>(`api/student-rewards/${id}`, studentRewardData, { signal }).then(r => r.data));
};
