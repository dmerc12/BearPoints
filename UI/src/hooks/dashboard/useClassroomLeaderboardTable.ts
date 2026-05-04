import type { FilterConfig, HeaderConfig } from '../../components';
import { useLeaderboardTable} from '../leaderboard';
import { useMemo, useEffect } from 'react';

export interface UseClassroomLeaderboardTableProps {
    teacherId: number;
    itemsPerPage?: number;
}

export interface UseClassroomLeaderboardTableReturn {
    modifiedFiltersConfig: FilterConfig[] | undefined;
    modifiedHeaderConfig: HeaderConfig;
}

export function useClassroomLeaderboardTable({
    teacherId,
    itemsPerPage = 10,
}: UseClassroomLeaderboardTableProps): UseClassroomLeaderboardTableReturn {
    const tableProps = useLeaderboardTable({ itemsPerPage });

    useEffect(() => {
        if (teacherId && tableProps.filters.teacherId !== teacherId.toString()) {
            tableProps.updateFilter('teacherId', teacherId.toString());
        }
    }, [teacherId, tableProps]);

    const modifiedFiltersConfig = useMemo(() => {
        return tableProps.filtersConfig?.filter(f => f.key !== 'teacherId');
    }, [tableProps]);

    const modifiedHeaderConfig = useMemo(() => ({
        ...tableProps.headerConfig,
        title: 'Classroom Leaderboard',
    }), [tableProps]);

    return { modifiedFiltersConfig, modifiedHeaderConfig };
}
