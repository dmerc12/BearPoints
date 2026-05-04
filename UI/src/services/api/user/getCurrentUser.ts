import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO } from '../../types';
import { api } from '../api';

export const getCurrentUser = async (signal?: AbortSignal): Promise<UserDTO> => {
    return withHealthAwareRetry(() => api.get<UserDTO>('api/users/me', { signal })
        .then(r => r.data));
};
