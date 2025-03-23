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
    respectful: boolean;
    responsible: boolean;
    safe: boolean;
    points: number;
    notes?: string;
}

export interface LeaderboardEntry {
    studentID: number;
    name: string;
    teacher: string;
    grade: string;
    points: number;
}

export interface BehaviorFormData {
    studentID: number;
    behaviors: {
        respectful: boolean;
        responsible: boolean;
        safe: boolean;
    },
    points: number;
    notes?: string;
}
