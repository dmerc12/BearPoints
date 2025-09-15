import { BragLog, GradeLevel, Student, UserDTO } from "./index";

export interface Teacher {
    id: number;
    grade: GradeLevel;
    user: UserDTO;
    students?: Student[];
    bragLogs?: BragLog[];
}
