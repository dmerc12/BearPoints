import { UserDTO } from './index';

export interface PaginatedUsers {
    users: UserDTO[];
    totalPages: number;
    totalUsers: number;
}
