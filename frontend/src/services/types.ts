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
