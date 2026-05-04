import type { FilterConfig, HeaderConfig } from '../../components';
import { useBragLogTable } from '../bragLog';
import { useAppSelector } from '../../store';
import { useMemo, useEffect } from 'react';
import { fullName } from '../../utils';

export interface UseClassroomBragLogsTableProps {
    teacherId: number;
    itemsPerPage?: number;
    showActions?: boolean;
}

export type UseClassroomBragLogsTableReturn = {
    modifiedFiltersConfig: FilterConfig[];
    modifiedHeaderConfig: HeaderConfig;
}

export function useClassroomBragLogsTable({ teacherId, itemsPerPage = 10 }
                                          : UseClassroomBragLogsTableProps): UseClassroomBragLogsTableReturn {
    const tableProps = useBragLogTable({ itemsPerPage });
    const { data: teachers } = useAppSelector(state => state.teachers);

    const teacherName = useMemo(() => {
        const teacher = teachers.find(t => t.id === teacherId);
        return teacher ? fullName(teacher): '';
    }, [teachers, teacherId]);

    useEffect(() => {
        if (teacherName && tableProps.filters.teacherName !== teacherName) {
            tableProps.updateFilter('teacherName', teacherName);
        }
    }, [teacherName, tableProps]);

    const modifiedFiltersConfig: FilterConfig[] = useMemo(() => {
        return tableProps.filtersConfig?.filter(f => f.key !== 'teacherName');
    }, [tableProps]);

    const modifiedHeaderConfig: HeaderConfig = useMemo(() => ({
        ...tableProps.headerConfig,
        title: 'Classroom Brag Logs'
    }), [tableProps]);

    return { modifiedFiltersConfig, modifiedHeaderConfig };
}
