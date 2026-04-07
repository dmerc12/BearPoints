import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { TeacherDTO } from '../../types';
import { api } from '../api';

export const createTeacher = async (teacherData: TeacherDTO,
                                    signal?: AbortSignal): Promise<TeacherDTO> => {
    return withHealthAwareRetry(() =>
        api.post<TeacherDTO>(`api/teachers`, teacherData, { signal })
            .then(r => r.data));
};
