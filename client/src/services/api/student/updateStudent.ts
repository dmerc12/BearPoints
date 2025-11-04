import { Student, withHealthAwareRetry, api } from '../../index';

export const updateStudent = async (id: number, studentData: Partial<Student>, signal?: AbortSignal): Promise<Student> => {
    return await withHealthAwareRetry(() =>
        api.patch<Student>(`api/students/${id}`, studentData, { signal }).then(r => r.data));
};
