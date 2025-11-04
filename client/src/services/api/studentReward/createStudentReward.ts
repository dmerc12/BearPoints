import { StudentReward, withHealthAwareRetry, api } from '../../index';

export const createStudentReward = async (studentRewardData: Partial<StudentReward>, signal?: AbortSignal): Promise<StudentReward> => {
    return await withHealthAwareRetry(() =>
        api.post<StudentReward>(`api/student-rewards`, studentRewardData, { signal }).then(r => r.data));
};
