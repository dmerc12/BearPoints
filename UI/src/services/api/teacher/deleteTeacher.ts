import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const deleteTeacher = async (id: number,
                                    signal?: AbortSignal): Promise<void> => {
    return withHealthAwareRetry(() =>
        api.delete(`api/teachers/${id}`, { signal }));
};
