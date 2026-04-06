import { Student, withHealthAwareRetry, api } from '../../index';

export const createStudent = async (studentData: Partial<Student>, signal?: AbortSignal): Promise<Student> => {
    return await withHealthAwareRetry(() =>
        api.post<Student>(`api/students`, studentData, { signal }).then(r => r.data));
};
