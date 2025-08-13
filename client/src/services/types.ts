export enum Timeframe {
    WEEK = 'WEEK',
    MONTH = 'MONTH',
    SEMESTER = 'SEMESTER',
    YEAR = 'YEAR'
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
    user: UserDTO | null;
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
    points: number;
    rank?: number;
    student: Person;
    teacher: Person;
    grade: string;
}

interface Person {
    id: number;
    firstName: string;
    lastName: string;
}

export interface Link {
    href: string;
}

export interface TeacherLinks {
    self: Link;
    bragLogs?: Link;
    students?: Link;
    user?: Link;
    teacher?: Link;
}

export interface TeacherResource {
    id: number;
    grade: string;
    _links: TeacherLinks;
}
