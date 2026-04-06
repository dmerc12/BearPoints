import { BehaviorTypeDTO } from './index';

export interface PaginatedBehaviorTypes {
    behaviorTypes: BehaviorTypeDTO[];
    totalPages: number;
    totalBehaviorTypes: number;
}
