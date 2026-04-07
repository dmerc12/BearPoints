import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { UserDTO, PagedResponseDTO } from '../../types';
import { api } from '../api';

export const getUsers = async (page = 0, size = 20, sort?: string,
                               signal?: AbortSignal): Promise<PagedResponseDTO<UserDTO>> => {
    let url = `api/users?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() => api.get<PagedResponseDTO<UserDTO>>(url, { signal })
        .then(r => r.data));
};
