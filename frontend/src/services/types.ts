export type Timeframe = 'week' | 'month' | 'semester' | 'year'

export interface Student {
    studentID: number;
    name: string;
    grade: string;
    teacher: string;
    teacherID: number;
    token: string;
    points: number;
}

export interface Teacher {
    teacherID: number;
    name: string;
    email: string;
    grade: string;
}

export interface StudentToken {
    studentID: number;
    name: string;
    teacherID: number;
    grade: string;
    token: string;
}

export interface BragLog {
    timestamp: string;
    studentID: number;
    teacherID: number;
    grade: string;
    brilliant: boolean;
    excelled: boolean;
    answered: boolean;
    read: boolean;
    sensationalWriting: boolean;
    points: number;
    notes?: string;
}

export interface LeaderboardEntry {
    studentID: number;
    name: string;
    teacher: string;
    grade: string;
    points: number;
    rank?: number;
}

export interface BehaviorFormData {
    studentID: number;
    teacherID: number;
    grade: string;
    behaviors: {
        brilliant: boolean;
        excelled: boolean;
        answered: boolean;
        read: boolean;
        sensationalWriting: boolean;
    },
    points: number,
    notes?: string;
}

export interface StudentsResponse {
    students: Student[];
    teachers: Teacher[];
}

export interface LeaderboardResponse {
    bragLogs: BragLog[];
    students: Student[];
}