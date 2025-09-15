import { UserDTO, withHealthAwareRetry, api } from '../../index';

export const updateUser = async (id: number, userData: Partial<UserDTO>, signal?: AbortSignal): Promise<UserDTO> => {
    return await withHealthAwareRetry(() =>
        api.patch<UserDTO>(`api/users/${id}`, userData, { signal }).then(r => r.data));
};
