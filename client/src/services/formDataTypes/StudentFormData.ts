import { PersonFormData } from './index.ts';

export interface StudentFormData extends PersonFormData {
    teacherId: string;
}
