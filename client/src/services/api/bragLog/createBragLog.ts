import { BragLog, withHealthAwareRetry, api } from '../../index';

export const createBragLog = async (bragLogData: Partial<BragLog>, signal?: AbortSignal): Promise<BragLog> => {
    return await withHealthAwareRetry(() =>
        api.post<BragLog>(`api/brag-logs`, bragLogData, { signal }).then(r => r.data));
};
