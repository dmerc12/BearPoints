import { fetchPaginated, PaginatedUsers, Role} from '../../index';

export const getUsersByRole = async (role: Role, page = 0, size = 100,
                                     sortQuery?: string, signal?: AbortSignal): Promise<PaginatedUsers> => {
    let url = `api/users/search/byRole?role=${role}&page=${page}&size=${size}&projection=userProjection`;
    if (sortQuery) {
        url += `&${sortQuery}`;
    }
    return await fetchPaginated<PaginatedUsers>(url,'users', signal);
};
