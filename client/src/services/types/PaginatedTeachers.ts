import { Teacher } from './index';

export interface PaginatedTeachers {
    teachers: Teacher[];
    totalPages: number;
    totalTeachers: number;
}
