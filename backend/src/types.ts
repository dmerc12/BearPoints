import type { auth } from 'firebase-admin';

declare global {
    namespace Express {
        interface Request {
            user?: auth.DecodedIdToken;
        }
    }
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

export interface Student {
    studentID: number;
    name: string;
    grade: string;
    teacher: string;
    token: string;
    points: number;
}
