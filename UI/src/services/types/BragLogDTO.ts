import { BehaviorTypeDTO } from './BehaviorTypeDTO';
import { GradeLevel } from './GradeLevel';

export interface BragLogDTO {
    // Request fields (sent by client for create/update)
    id?: number | null;
    studentId: number;
    behaviorIds: number[];
    notes?: string | null;
    submitterName: string;
    submitterUserId?: number | null;
    // Response fields (server returns, client shouldn't send)
    teacherId?: number | null;
    studentName?: string | null;
    teacherName?: string | null;
    grade?: GradeLevel | null;
    behaviors?: BehaviorTypeDTO[] | null;
    pointsGenerated?: number | null;
    timestamp?: string | null;
}
