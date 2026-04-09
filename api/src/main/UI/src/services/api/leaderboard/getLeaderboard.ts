import { Timeframe, GradeLevel, PagedResponseDTO, LeaderboardEntryDTO } from '../../types';
import { withHealthAwareRetry } from "../withHealthAwareRetry.ts";
import { api } from '../api';

export const getLeaderboard = async (timeframe: Timeframe = Timeframe.WEEK,
                                     teacherId?: number, grade?: GradeLevel, page: number = 0, size: number = 20,
                                     sort?: string, signal?: AbortSignal)
    : Promise<PagedResponseDTO<LeaderboardEntryDTO>> => {
    const searchParams = new URLSearchParams();
    searchParams.append('timeframe', timeframe);
    if (teacherId !== undefined) searchParams.append('teacherId', String(teacherId));
    if (grade !== undefined) searchParams.append('grade', grade);
    searchParams.append('page', String(page));
    searchParams.append('size', String(size));
    if (sort) searchParams.append('sort', String(sort));
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<LeaderboardEntryDTO>>(`api/leaderboard?${searchParams}`, { signal })
            .then(r => r.data));
};
