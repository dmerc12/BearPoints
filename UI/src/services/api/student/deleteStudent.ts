import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteStudent = async (id: number, signal?: AbortSignal): Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/students/${id}`, { signal }));
};
