import { GradeLevel } from './GradeLevel';
import { PersonDTO } from './PersonDTO';

export interface LeaderboardEntryDTO {
    points: number;
    rank?: number;
    student: PersonDTO;
    teacher: PersonDTO;
    grade: GradeLevel;
}
