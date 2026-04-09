import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BragLogDTO } from '../../types';
import { api } from '../api';

export const updateBragLog = async (id: number, bragLogData: BragLogDTO,
                                    signal?: AbortSignal): Promise<BragLogDTO> => {
    return withHealthAwareRetry(() =>
        api.put<BragLogDTO>(`api/brags/${id}`, bragLogData, { signal })
            .then(r => r.data));
};
