import { useStudentBragLogsTable } from '../../hooks';
import { BragLogTable } from '../bragLogs';

interface StudentBragLogsTableProps {
    studentName: string;
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
}

export default function StudentBragLogsTable({ studentName, itemsPerPage = 10, size = 'm' }: StudentBragLogsTableProps) {
    const { modifiedFiltersConfig, modifiedHeaderConfig } = useStudentBragLogsTable({ studentName, itemsPerPage});

    return (
        <BragLogTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
        />
    );
}
