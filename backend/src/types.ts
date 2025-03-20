import { Request } from 'express-serve-static-core';

// Leaderboard types
export type Timeframe = 'week' | 'month' | 'semester' | 'year' | 'custom';

export interface BehaviorLog {
    timestamp: string;
    studentID: number;
    respectful: boolean;
    responsible: boolean;
    safe: boolean;
    points: number;
    notes?: string;
}

export interface Student {
    studentID: number;
    name: string;
    grade: string;
    teacher: string;
}

export interface LeaderboardEntry {
    studentID: number;
    name: string;
    teacher: string;
    points: number;
}

export interface StudentsMap {
    name: string;
    teacher: string;
}

// Auth types
export interface DecodedToken {
    uid: string;
    email?: string;
    [ key: string ]: any;
}

export interface AuthenticatedRequest extends Request {
    user?: DecodedToken;
}
