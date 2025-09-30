import { PaginatedLeaderboardEntries, Timeframe, fetchPaginated } from '../../index';

export const getLeaderboard = async (timeframe: Timeframe, page: number = 0,
                                     size: number = 20, sort?: string,
                                     signal?: AbortSignal): Promise<PaginatedLeaderboardEntries> => {
    let url = `api/leaderboard?timeframe=${timeframe}&page=${page}&size=${size}`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedLeaderboardEntries>(
        url,
        'leaderboardEntries',
        signal
    );
};
