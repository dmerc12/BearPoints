import { BragLogRequest, withHealthAwareRetry, api } from '../../index';

export const submitPublicBragLog = async (data: BragLogRequest, signal?: AbortSignal) => {
    return withHealthAwareRetry(() =>
        api.post('api/public/brag-logs', data, { signal }));
};
