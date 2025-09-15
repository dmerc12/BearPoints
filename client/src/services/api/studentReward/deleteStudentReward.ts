import { withHealthAwareRetry, api } from '../index';

export const deleteStudentReward = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/student-rewards/${id}`, { signal }));
};
