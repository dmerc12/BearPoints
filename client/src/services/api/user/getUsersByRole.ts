import { fetchPaginated, PaginatedUsers, Role} from '../../index';

export const getUsersByRole = async (role: Role, page = 0, size = 100,
                                     sort?: string, signal?: AbortSignal): Promise<PaginatedUsers> => {
    let url = `api/users/search/byRole?role=${role}&page=${page}&size=${size}&projection=userProjection`;
    if (sort) {
        url += `&sort=${sort}`;
    }
    return await fetchPaginated<PaginatedUsers>(
        url,
        'users',
        signal
    );
};
