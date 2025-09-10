import { PersonFormData } from './index';

export interface StudentFormData extends PersonFormData {
    teacherId: string;
}
