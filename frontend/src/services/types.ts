export type Timeframe = 'WEEK' | 'MONTH' | 'SEMESTER' | 'YEAR'

export interface UserDTO {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    teacherId?: number;
    studentId?: number;
}

export interface Student {
    id: number;
    name: string;
    grade: string;
    points: number;
    token: string;
    teacher: {
        id: number;
        name: string;
    };
}

export interface Teacher {
    id: number;
    name: string;
    grade: string;
}

export interface BehaviorType {
    id: number;
    name: string;
    pointValue: number;
    active: boolean;
}

export interface BragLog {
    id: number;
    timestamp: string;
    student: {
        id: number;
        name: string;
    };
    teacher: {
        id: number;
        name: string;
    };
    behaviors: BehaviorType[];
    pointsGenerated: number;
    notes?: string;
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
