import { Teacher, withHealthAwareRetry, api } from '../../index';

export const updateTeacher = async (id: number, teacherData: Partial<Teacher>, signal?: AbortSignal): Promise<Teacher> => {
    return await withHealthAwareRetry(() =>
        api.patch<Teacher>(`api/teachers/${id}`, teacherData, { signal }).then(r => r.data));
};
