import { TeacherDTO } from './TeacherDTO';
import { UserDTO } from './UserDTO';

export interface StudentDTO {
    id?: number | null;
    user: UserDTO;
    teacher: TeacherDTO;
    points?: number;
    token?: string;
}
