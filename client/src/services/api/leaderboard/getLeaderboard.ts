import { PaginatedLeaderboardEntries, Timeframe, fetchPaginated } from '../../index';

export const getLeaderboard = async (timeframe: Timeframe, page: number = 0,
                                     size: number = 20, signal?: AbortSignal): Promise<PaginatedLeaderboardEntries> => {
    return await fetchPaginated<PaginatedLeaderboardEntries>(
        `api/leaderboard?timeframe=${timeframe}&page=${page}&size=${size}`,
        'leaderboardEntries',
        signal
    );
};
