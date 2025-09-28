import { LeaderboardEntry } from './index';

export interface PaginatedLeaderboardEntries {
    leaderboardEntries: LeaderboardEntry[];
    totalPages: number;
    totalLeaderboardEntries: number;
}
