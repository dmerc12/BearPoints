import { BragLogDTO, withHealthAwareRetry, api } from '../../index';

export const createBragLog = async (bragLogData: Partial<BragLogDTO>, signal?: AbortSignal): Promise<BragLogDTO> => {
    return await withHealthAwareRetry(() =>
        api.post<BragLogDTO>(`api/brag-logs`, bragLogData, { signal }).then(r => r.data));
};
