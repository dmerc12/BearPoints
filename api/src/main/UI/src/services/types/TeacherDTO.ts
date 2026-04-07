import { GradeLevel } from "./GradeLevel";
import { UserDTO } from "./UserDTO";

export interface TeacherDTO {
    id?: number | null;
    grade: GradeLevel;
    user: UserDTO;
}
