import { UserDTO, fetchResource } from '../../index';

export const getCurrentUser = async (signal?: AbortSignal): Promise<UserDTO> => {
    return fetchResource('api/users/me', signal);
};
