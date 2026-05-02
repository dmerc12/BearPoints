import { useClassroomBragLogsTable } from '../../hooks';
import { BragLogTable } from '../bragLogs';

interface ClassroomBragLogsTableProps {
    teacherId: number;
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
}

export default function ClassroomBragLogsTable({ teacherId, itemsPerPage = 10, size = 'm' }: ClassroomBragLogsTableProps) {
    const { modifiedFiltersConfig, modifiedHeaderConfig } = useClassroomBragLogsTable({ teacherId, itemsPerPage });

    return (
        <BragLogTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
        />
    );
}
