import { BragLog, Teacher, UserDTO } from './index';

export interface Student {
    id: number;
    points: number;
    token: string;
    user: UserDTO;
    teacher: Teacher;
    bragLogs?: BragLog[];
}
