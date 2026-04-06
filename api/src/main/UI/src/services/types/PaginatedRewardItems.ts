import { RewardItem } from './index';

export interface PaginatedRewardItems {
    rewardItems: RewardItem[];
    totalPages: number;
    totalRewardItems: number;
}
