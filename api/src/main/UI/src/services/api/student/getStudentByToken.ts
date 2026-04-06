import { Student, fetchResource } from '../../index';

export const getStudentByToken = async (token: string, signal?: AbortSignal): Promise<Student> => {
    return await fetchResource<Student>(`api/students/search/findByToken?token=${token}&projection=studentProjection`, signal);
};
