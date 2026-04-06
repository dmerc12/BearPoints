import { Person } from './index';

export interface LeaderboardEntry {
    points: number;
    rank?: number;
    student: Person;
    teacher: Person;
    grade: string;
}
