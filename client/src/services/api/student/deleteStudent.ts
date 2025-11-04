import { withHealthAwareRetry, api } from '../../index';

export const deleteStudent = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/students/${id}`, { signal }));
};
