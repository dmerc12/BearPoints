import { Student, RewardItem } from './index';

export interface StudentReward {
    id: number;
    redeemedAt: string;
    student: Student;
    rewardItem: RewardItem;
}
