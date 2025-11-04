import { BragLog, withHealthAwareRetry, api } from '../../index';

export const updateBragLog = async (id: number, bragLogData: Partial<BragLog>, signal?: AbortSignal): Promise<BragLog> => {
    return await withHealthAwareRetry(() =>
        api.patch<BragLog>(`api/brag-logs/${id}`, bragLogData, { signal }).then(r => r.data));
};
