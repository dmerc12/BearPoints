import { LeaderboardEntry, Timeframe, fetchResource } from '../../index';

export const getLeaderboard = async (timeframe: Timeframe, signal?: AbortSignal): Promise<LeaderboardEntry[]> => {
    return await fetchResource<LeaderboardEntry[]>(`api/leaderboard?timeframe=${timeframe}`, signal);
};
