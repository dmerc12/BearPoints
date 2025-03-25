export type Timeframe = 'week' | 'month' | 'semester' | 'year'

export interface Student {
    studentID: number;
    name: string;
    grade: string;
    teacher: string;
    token: string;
    points: number;
}

export interface BehaviorLog {
    timestamp: string;
    studentID: number;
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
    behaviors: {
        brilliant: boolean;
        excelled: boolean;
        answered: boolean;
        read: boolean;
        sensationalWriting: boolean;
    },
    points: number;
    notes?: string;
}
