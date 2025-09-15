import { Student } from './Student';

export interface PaginatedStudents {
    students: Student[];
    totalPages: number;
    totalStudents: number;
}
