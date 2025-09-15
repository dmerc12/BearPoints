import { withHealthAwareRetry, api } from '../index';

export const deleteUser = async (id: number, signal?: AbortSignal): Promise<void> => {
    return await withHealthAwareRetry(() =>
        api.delete(`api/users/${id}`, { signal }));
};
