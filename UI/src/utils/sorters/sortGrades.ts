import { GradeLevel } from '../../services';

export function sortGrades(grades: (GradeLevel | string)[]) {
    const orderMap: Record<string, number> = {
        [GradeLevel.PRE_K]: 0,
        [GradeLevel.K]: 1,
        [GradeLevel.FIRST]: 2,
        [GradeLevel.SECOND]: 3,
        [GradeLevel.THIRD]: 4,
        [GradeLevel.FOURTH]: 5,
    };
    return [...grades].sort((a, b) => {
       const orderA = orderMap[a as GradeLevel] ?? 999;
       const orderB = orderMap[b as GradeLevel] ?? 999;
       return orderA - orderB;
    });
}
