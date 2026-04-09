import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BragLogDTO } from '../../types';
import { api } from '../api';

export const createBragLog = async (bragLogData: BragLogDTO,
                                    signal?: AbortSignal): Promise<BragLogDTO> => {
    return withHealthAwareRetry(() =>
        api.post<BragLogDTO>(`api/brags`, bragLogData, { signal })
            .then(r => r.data));
};
