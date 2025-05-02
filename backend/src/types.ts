import type { auth } from 'firebase-admin';

declare global {
    namespace Express {
        interface Request {
            user?: auth.DecodedIdToken;
        }
    }
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

export type BodyType = {
    studentID: number;
    teacherID: number;
    grade: string;
    behaviors: {
        brilliant: boolean;
        excelled: boolean;
        answered: boolean;
        read: boolean;
        sensationalWriting: boolean;
    };
    notes?: string;
};

export type BragRow = {
    timestamp: string;
    studentID: number;
    teacherID: number;
    grade: string;
    brilliant: boolean | string;
    excelled: boolean | string;
    answered: boolean | string;
    read: boolean | string;
    sensationalWriting: boolean | string;
    points: number;
    notes?: string;
};
