import { PersonFormData } from './index.ts';

export interface TeacherFormData extends PersonFormData {
    grade: string | null;
}
