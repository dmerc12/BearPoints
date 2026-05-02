import { useClassroomStudentsTable } from '../../hooks';
import { CreateStudentModal } from '../students';
import { StudentTable } from '../index';
import { useMemo } from 'react';

interface ClassroomStudentsTableProps {
    teacherId: number;
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
    showActions?: boolean;
}

export default function ClassroomStudentsTable({
    teacherId,
    itemsPerPage = 10,
    size = 'm',
    showActions = false
}: ClassroomStudentsTableProps) {
    const { studentTableProps, modifiedFiltersConfig, modifiedHeaderConfig } =
        useClassroomStudentsTable({ teacherId, itemsPerPage, size, showActions });

    const customCreateModal = useMemo(() => (
        <CreateStudentModal
            show={studentTableProps.showCreateModal}
            onCancel={studentTableProps.handleCloseModals}
            onSuccess={studentTableProps.handleSuccess}
            defaultTeacherId={teacherId}
        />
    ), [studentTableProps, teacherId]);

    return (
        <StudentTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
            customCreateModal={customCreateModal}
        />
    );
}