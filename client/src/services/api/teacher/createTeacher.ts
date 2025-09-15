import { Teacher, withHealthAwareRetry, api } from '../../index';

export const createTeacher = async (teacherData: Partial<Teacher>, signal?: AbortSignal): Promise<Teacher> => {
    return await withHealthAwareRetry(() =>
        api.post<Teacher>(`api/teachers`, teacherData, { signal }).then(r => r.data));
};
