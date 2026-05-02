import type { FilterConfig, HeaderConfig } from '../../components';
import { useBragLogTable } from '../bragLog';
import { useMemo, useEffect } from 'react';

export interface UseStudentBragLogTableProps {
    studentName: string;
    itemsPerPage?: number;
}

export interface UseStudentBragLogTableReturn {
    modifiedFiltersConfig: FilterConfig[];
    modifiedHeaderConfig: HeaderConfig;
}

export function useStudentBragLogsTable({ studentName, itemsPerPage = 10 }
                                        : UseStudentBragLogTableProps): UseStudentBragLogTableReturn {
    const tableProps = useBragLogTable({ itemsPerPage });

    useEffect(() => {
        if (studentName && tableProps.filters.studentName !== studentName) {
            tableProps.updateFilter('studentName', studentName);
        }
    }, [studentName, tableProps]);

    const modifiedFiltersConfig: FilterConfig[] = useMemo(() => {
        return tableProps.filtersConfig?.filter(f => f.key !== 'studentName');
    }, [tableProps]);

    const modifiedHeaderConfig: HeaderConfig = useMemo(() => ({
        ...tableProps.headerConfig,
        title: 'My Bear Brags',
        showCreateButton: false,
    }), [tableProps]);

    return { modifiedFiltersConfig, modifiedHeaderConfig };
}