import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO } from '../../types';
import { api } from '../api';

export const createUser = async (userData: UserDTO, signal?: AbortSignal): Promise<UserDTO> => {
    return withHealthAwareRetry(() => api.post<UserDTO>(`api/users`, userData, { signal })
        .then(r => r.data));
};
