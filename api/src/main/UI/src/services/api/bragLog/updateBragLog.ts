import { BragLogDTO, withHealthAwareRetry, api } from '../../index';

export const updateBragLog = async (id: number, bragLogData: Partial<BragLogDTO>, signal?: AbortSignal): Promise<BragLogDTO> => {
    return await withHealthAwareRetry(() =>
        api.patch<BragLogDTO>(`api/brag-logs/${id}`, bragLogData, { signal }).then(r => r.data));
};
