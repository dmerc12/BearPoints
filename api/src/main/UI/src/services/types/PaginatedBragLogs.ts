import { BragLogDTO } from './index';

export interface PaginatedBragLogs {
    bragLogs: BragLogDTO[];
    totalPages: number;
    totalBragLogs: number;
}
