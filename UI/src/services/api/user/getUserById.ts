import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO } from '../../types';
import { api } from '../api';

export const getUserById = async (id: number, signal?: AbortSignal)
    : Promise<UserDTO> => {
        return withHealthAwareRetry(() => api.get<UserDTO>(`api/users/${id}`, { signal })
            .then(r => r.data));
};
