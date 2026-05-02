import { useSubmitterBragLogsTable } from '../../hooks';
import { BragLogTable } from '../bragLogs';

interface SubmitterBragLogsTableProps {
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
}

export default function SubmitterBragLogsTable({ itemsPerPage = 10, size = 'm' }: SubmitterBragLogsTableProps) {
    const { modifiedFiltersConfig, modifiedHeaderConfig } = useSubmitterBragLogsTable({ itemsPerPage });

    return (
        <BragLogTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
        />
    );
}
