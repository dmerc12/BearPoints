import type { FilterConfig, HeaderConfig } from '../../components';
import { useAppSelector } from '../../store';
import { useBragLogTable } from '../bragLog';
import { useMemo, useEffect } from 'react';
import { fullName } from '../../utils';

export interface UseSubmitterBragLogsTableProps {
    itemsPerPage?: number;
}

export interface UseSubmitterBragLogsTableReturn {
    modifiedFiltersConfig: FilterConfig[];
    modifiedHeaderConfig: HeaderConfig;
}

export function useSubmitterBragLogsTable({ itemsPerPage = 10 }: UseSubmitterBragLogsTableProps): UseSubmitterBragLogsTableReturn {
    const currentUser = useAppSelector(state => state.user.data);

    const tableProps = useBragLogTable({ itemsPerPage });

    const submitterName = useMemo(() => {
        return currentUser ? fullName(currentUser) : '';
    }, [currentUser]);

    useEffect(() => {
        if (submitterName && tableProps.filters.submitterName !== submitterName) {
            tableProps.updateFilter('submitterName', submitterName);
        }
    }, [submitterName, tableProps]);

    const modifiedFiltersConfig: FilterConfig[] = useMemo(() => {
        return tableProps.filtersConfig?.filter(f => f.key !== 'submitterName');
    }, [tableProps]);

    const modifiedHeaderConfig: HeaderConfig = useMemo(() => ({
        ...tableProps.headerConfig,
        title: 'My Submitted Bear Brags',
    }), [tableProps]);

    return { modifiedFiltersConfig, modifiedHeaderConfig };
}