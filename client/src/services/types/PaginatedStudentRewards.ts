import { StudentReward } from './index';

export interface PaginatedStudentRewards {
    studentRewards: StudentReward[];
    totalPages: number;
    totalStudentRewards: number;
}
