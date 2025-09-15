export interface BragLogRequest {
    studentId: number;
    teacherId: number;
    behaviorIds: number[];
    notes?: string;
}
