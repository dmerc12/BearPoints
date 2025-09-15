import { fetchPaginated, PaginatedUsers, Role} from '../../index';

export const getUsersByRole = async (role: Role, page = 0, size = 100, signal?: AbortSignal): Promise<PaginatedUsers> => {
    return await fetchPaginated<PaginatedUsers>(
        `api/users/search/byRole?role=${role}&page=${page}&size=${size}&projection=userProjection`,
        'users',
        signal
    );
};
