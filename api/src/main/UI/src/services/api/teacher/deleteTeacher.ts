import { withHealthAwareRetry, api } from '../index';

export const deleteTeacher = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/teachers/${id}`, { signal }));
};
