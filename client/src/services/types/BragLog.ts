import { Student, Teacher, BehaviorType } from './index';

export interface BragLog {
    id: number;
    student: Student;
    teacher: Teacher;
    behaviors: BehaviorType[];
    pointsGenerated: number;
    notes?: string;
    timestamp: string;
}
