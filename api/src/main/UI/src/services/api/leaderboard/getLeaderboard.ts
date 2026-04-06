import { PaginatedLeaderboardEntries, Timeframe, fetchPaginated } from '../../index';

export const getLeaderboard = async (timeframe: Timeframe, page: number = 0,
                                     size: number = 20, sortQuery?: string,
                                     signal?: AbortSignal): Promise<PaginatedLeaderboardEntries> => {
    let url = `api/leaderboard?timeframe=${timeframe}&page=${page}&size=${size}`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedLeaderboardEntries>(url,'leaderboardEntries', signal);
};
