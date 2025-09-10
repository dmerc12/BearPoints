import { PersonFormData } from './index';

export interface TeacherFormData extends PersonFormData {
    grade: string | null;
}
