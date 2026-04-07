import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO } from '../../types';
import { api } from '../api';

export const updateUser = async (id: number, userData: UserDTO, signal?: AbortSignal)
    : Promise<UserDTO> => {
        return withHealthAwareRetry(() =>
            api.put<UserDTO>(`api/users/${id}`, userData, { signal })
                .then(r => r.data));
};
