import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteStudentReward = async (id: number, signal?: AbortSignal)
    : Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/rewards/${id}`, { signal }));
};
