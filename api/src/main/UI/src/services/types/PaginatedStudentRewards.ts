import { StudentRewardDTO } from './index';

export interface PaginatedStudentRewards {
    studentRewards: StudentRewardDTO[];
    totalPages: number;
    totalStudentRewards: number;
}
