export interface StudentRewardDTO {
    // Request fields (sent by client for create)
    id?: number | null;
    studentId: number;
    itemId: number;
    // Response fields (server returns)
    timestamp?: string | null;
    studentName?: string | null;
    itemName?: string | null;
    pointsUsed?: number | null;
}
