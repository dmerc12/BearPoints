import type { FilterConfig, HeaderConfig } from '../../components';
import { useStudentTable } from '../student';
import { useMemo, useEffect } from 'react';

export interface UseClassroomStudentsTableProps {
    teacherId: number;
    itemsPerPage?: number;
    showActions?: boolean;
}

export interface UseClassroomStudentsTableReturn {
    studentTableProps: ReturnType<typeof useStudentTable>;
    modifiedFiltersConfig: FilterConfig[];
    modifiedHeaderConfig: HeaderConfig;
    showActions: boolean;
}

export function useClassroomStudentsTable({
    teacherId,
    itemsPerPage = 10,
    showActions = true,
}: UseClassroomStudentsTableProps): UseClassroomStudentsTableReturn {
    const studentTableProps = useStudentTable({ itemsPerPage });

    useEffect(() => {
        if (teacherId && studentTableProps.filters.teacherId !== teacherId.toString()) {
            studentTableProps.updateFilter('teacherId', teacherId.toString());
        }
    }, [teacherId, studentTableProps]);

    const modifiedFiltersConfig: FilterConfig[] = useMemo(() => {
        return studentTableProps.filtersConfig?.filter(f => f.key !== 'teacherId');
    }, [studentTableProps]);

    const modifiedHeaderConfig: HeaderConfig = useMemo(() => ({
        ...studentTableProps.headerConfig,
        title: 'My Students',
        showCreateButton: showActions && studentTableProps.isAuthorized,
        createButtonText: 'Add Student',
    }), [studentTableProps, showActions]);

    return {
        studentTableProps,
        modifiedFiltersConfig,
        modifiedHeaderConfig,
        showActions
    };
}
