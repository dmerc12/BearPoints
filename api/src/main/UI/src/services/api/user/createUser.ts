import { UserDTO, withHealthAwareRetry, api } from '../../index';

export const createUser = async (userData: Partial<UserDTO>, signal?: AbortSignal): Promise<UserDTO> => {
    return await withHealthAwareRetry(() =>
        api.post<UserDTO>(`api/users`, userData, { signal }).then(r => r.data));
};
