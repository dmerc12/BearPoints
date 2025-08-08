export enum Timeframe {
    'WEEK',
    'MONTH',
    'SEMESTER',
    'YEAR'
}

export enum GradeLevel {
    PRE_K = 'PRE_K',
    K = 'K',
    FIRST = 'FIRST',
    SECOND = 'SECOND',
    THIRD = 'THIRD',
    FOURTH = 'FOURTH'
}

export enum Role {
    STUDENT = 'STUDENT',
    TEACHER = 'TEACHER',
    ADMIN = 'ADMIN'
}

export interface UserDTO {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    teacherId?: number;
    studentId?: number;
}

export interface Student {
    id: number;
    points: number;
    token: string;
    user: UserDTO;
    teacher: Teacher;
    bragLogs: BragLog[];
}

export interface Teacher {
    id: number;
    grade: string;
    user: UserDTO;
    students: Student[];
    bragLogs: BragLog[];
}

export interface BehaviorType {
    id: number;
    name: string;
    pointValue: number;
    active: boolean;
}

export interface BragLog {
    id: number;
    student: Student;
    teacher: Teacher;
    behaviors: BehaviorType[];
    pointsGenerated: number;
    notes?: string;
    timestamp: string;

}

export interface BragLogRequest {
    studentId: number;
    teacherId: number;
    behaviorIds: number[];
    notes?: string;
}

export interface LeaderboardEntry {
    studentId: number;
    studentName: string;
    teacherName: string;
    grade: string;
    points: number;
    rank?: number;
}
