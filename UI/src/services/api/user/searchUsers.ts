import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO, PagedResponseDTO } from '../../types';
import { api } from '../api';

export const searchUsers = async (params: { email?: string, firstName?: string,
    lastName?: string, role?: string, page?: number, size?: number, sort?: string },
                                  signal?: AbortSignal): Promise<PagedResponseDTO<UserDTO>> => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) searchParams.append(key, String(value));
    });
    return withHealthAwareRetry(() => api.get<PagedResponseDTO<UserDTO>>(`api/users/search?${searchParams}`, { signal })
        .then(r => r.data));
};
