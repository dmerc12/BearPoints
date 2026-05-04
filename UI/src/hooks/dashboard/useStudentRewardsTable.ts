import type { FilterConfig, HeaderConfig } from '../../components';
import { useStudentRewardTable } from '../studentReward';
import { useMemo, useEffect } from 'react';

export interface UseStudentRewardsTableProps {
    studentName: string;
    itemsPerPage?: number;
}

export interface UseStudentRewardsTableReturn {
    modifiedFiltersConfig: FilterConfig[];
    modifiedHeaderConfig: HeaderConfig;
}

export function useStudentRewardsTable({ studentName, itemsPerPage = 10 }
                                       : UseStudentRewardsTableProps): UseStudentRewardsTableReturn {
    const tableProps = useStudentRewardTable({ itemsPerPage });

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
        title: 'My Redeemed Rewards',
        showCreateButton: false,
    }), [tableProps]);

    return { modifiedFiltersConfig, modifiedHeaderConfig };
}
