import { fetchBragLogs, RootState } from '../../store';
import { BragLog } from '../../services';
import { useTable } from '../index';

export function useBragLogTable() {
    return useTable<BragLog, { studentFilter: string; teacherFilter: string; dateFilter: string }>({
        fetchAction: fetchBragLogs,
        selector: (state: RootState) => state.bragLogs,
        initialFilters: {
            studentFilter: '',
            teacherFilter: '',
            dateFilter: ''
        }
    });
}