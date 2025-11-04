import { BragLog } from './index';

export interface PaginatedBragLogs {
    bragLogs: BragLog[];
    totalPages: number;
    totalBragLogs: number;
}
