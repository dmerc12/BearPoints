import { Role } from './index';

export interface UserDTO {
    id?: number | null;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
}
