import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { BragLogDTO } from '../../types';
import { api } from '../api';

export const getBragLogById = async (id: number, signal?: AbortSignal)
    : Promise<BragLogDTO> => {
    return withHealthAwareRetry(() => api.get<BragLogDTO>(`api/brags/${id}`, { signal })
        .then(r => r.data));
};
