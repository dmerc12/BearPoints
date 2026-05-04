import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { TeacherDTO } from '../../types';
import { api } from '../api';

export const updateTeacher = async (id: number, teacherData: TeacherDTO,
                                    signal?: AbortSignal): Promise<TeacherDTO> => {
    return withHealthAwareRetry(() =>
        api.put<TeacherDTO>(`api/teachers/${id}`, teacherData, { signal })
            .then(r => r.data));
};
