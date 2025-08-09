import {GradeLevel} from '../services/types';

export function formatGrade(grade: GradeLevel | string): string {
    switch (grade) {
        case GradeLevel.PRE_K:
            return 'Pre-K';
        case GradeLevel.K:
            return 'K';
        case GradeLevel.FIRST:
            return '1';
        case GradeLevel.SECOND:
            return '2';
        case GradeLevel.THIRD:
            return '3';
        case GradeLevel.FOURTH:
            return '4';
        default:
            return grade;
    }
}
