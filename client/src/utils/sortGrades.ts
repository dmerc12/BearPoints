import { GradeLevel } from '../services/types';

export function sortGrades(grades: (GradeLevel | string)[]) {
    return [...grades].sort((a, b) => {
       if (a === GradeLevel.PRE_K) return -1;
       if (b === GradeLevel.PRE_K) return 1;
       if (a === GradeLevel.K) return -1;
       if (b === GradeLevel.K) return 1;
       return a.localeCompare(b);
    });
}
