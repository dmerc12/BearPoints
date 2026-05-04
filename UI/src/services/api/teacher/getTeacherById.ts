import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { TeacherDTO } from '../../types';
import { api } from '../api';

export const getTeacherById = async(id: number,
                                    signal?: AbortSignal): Promise<TeacherDTO> => {
    return withHealthAwareRetry(() => api.get<TeacherDTO>(`api/teachers/${id}`, { signal })
        .then(r => r.data));
};
