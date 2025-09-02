import { Role } from '../services/types';

export function formatRole(role: Role): string {
    switch (role) {
        case Role.ADMIN:
            return 'Administrator';
        case Role.TEACHER:
            return 'Teacher';
        case Role.STUDENT:
            return 'Student';
        default:
            return role;
    }
}
