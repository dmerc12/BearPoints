import { Role } from '../../services';

export function formatRole(role: Role): string {
    switch (role) {
        case Role.ADMIN:
            return 'Administrator';
        case Role.TEACHER:
            return 'Teacher';
        case Role.STUDENT:
            return 'Student';
        case Role.STAFF:
            return 'Staff';
        case Role.PARA:
            return 'Paraprofessional';
        default:
            return role;
    }
}
