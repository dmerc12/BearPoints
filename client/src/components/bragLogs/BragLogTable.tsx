import {
    CreateBragLogModal, EditBragLogModal, DeleteBragLogModal, TableColumn, BaseTable, TextFilter
} from '../index';
import { formatBragLogDate, getBragLogPointsVariant, fullName } from '../../utils';
import { Row, Button, Col, ButtonGroup, Badge } from 'react-bootstrap';
import { BragLog, Role } from '../../services';
import { useBragLogTable } from '../../hooks';
import { useAppSelector } from '../../store';
import { useMemo, useEffect } from 'react';

interface BragLogTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function BragLogTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: BragLogTableProps) {
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin =  useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher =  useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);

    const {
        data: bragLogs, loading, error, filters, updateFilter, resetFilters, showCreateModal,
        editingItem: editingBragLog, deletingItem: deletingBragLog, handleCreateItem: handleCreateBragLog,
        handleEditItem: handleEditBragLog, handleDeleteItem: handleDeleteBragLog,
        handleCloseModals, retryFetch, handleSuccess
    } = useBragLogTable();

    useEffect(() => {
        return () => {
            resetFilters();
        };
    }, [resetFilters]);

    const filteredBragLogs = useMemo(() => {
        if (!bragLogs.length) return [];
        const studentFilter = filters.studentFilter.toLowerCase();
        const teacherFilter = filters.teacherFilter.toLowerCase();
        const dateFilter = filters.dateFilter;
        return bragLogs.filter((bragLog: BragLog) => {
            const studentName = fullName(bragLog.student).toLowerCase();
            const teacherName = fullName(bragLog.teacher).toLowerCase();
            const logDate = formatBragLogDate(bragLog.timestamp);
            const studentMatches = !studentFilter || studentName.includes(studentFilter);
            const teacherMatches = !teacherFilter || teacherName.includes(teacherFilter);
            const dateMatches = !dateFilter || logDate.includes(dateFilter);
            return studentMatches && teacherMatches && dateMatches;
        });
    }, [bragLogs, filters]);

    const columns: TableColumn<BragLog>[] = useMemo(() => [
        {
            key: 'student',
            header: 'Student',
            render: (bragLog: BragLog) => fullName(bragLog.student)
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (bragLog: BragLog) => fullName(bragLog.teacher)
        },
        {
            key: 'behaviors',
            header: 'Behaviors',
            render: (bragLog: BragLog) => bragLog.behaviors.map(b => b.name).join(', ')
        },
        {
            key: 'points',
            header: 'Points',
            render: (bragLog: BragLog) => (
                <Badge bg={getBragLogPointsVariant(bragLog.pointsGenerated)}>
                    {bragLog.pointsGenerated}
                </Badge>
            )
        },
        {
            key: 'date',
            header: 'Date',
            render: (bragLog: BragLog) => formatBragLogDate(bragLog.timestamp)
        },
        ...(isAdmin) ? [{
            key: 'actions',
            header: 'Actions',
            render: (bragLog: BragLog) => (
                <ButtonGroup size='sm'>
                    <Button variant='outline-primary'
                            onClick={() => handleEditBragLog(bragLog)}
                    >
                        Edit
                    </Button>
                    <Button variant='danger'
                            onClick={() => handleDeleteBragLog(bragLog)}
                    >
                        Delete
                    </Button>
                </ButtonGroup>
            )
        }] : []
    ], [isAdmin, handleEditBragLog, handleDeleteBragLog]);

    const renderFilters = () => {
        if (!showFilters) return null;
        return (
            <Row className='mb-4 g-3'>
                <Col md={4}>
                    <TextFilter value={filters.studentFilter}
                                onChange={(value) => updateFilter('studentFilter', value)}
                                label='Search Students'
                                placeholder='Search by student name...'
                    />
                    <TextFilter value={filters.teacherFilter}
                                onChange={(value) => updateFilter('teacherFilter', value)}
                                label='Search Teachers'
                                placeholder='Search by teacher name...'
                    />
                </Col>
                <Col md={4}>
                    <TextFilter value={filters.dateFilter}
                                onChange={(value) => updateFilter('dateFilter', value)}
                                label='Search by Date'
                                placeholder='MM/DD/YYYY'
                    />
                </Col>
            </Row>
        );
    };

    const renderHeader = () => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing {filteredBragLogs.length} of {bragLogs.length} brag logs</span>
                <Button variant='primary'
                        onClick={handleCreateBragLog}
                        className='me-2'
                        size={size === 's' ? 'sm' : undefined}
                >
                    Create Brag Log
                </Button>
            </div>
        );
    };

    return (
        <>
            <BaseTable<BragLog>
                data={filteredBragLogs}
                loading={loading}
                error={error}
                columns={columns}
                itemsPerPage={itemsPerPage}
                renderFilters={renderFilters}
                renderHeader={renderHeader}
                onRetry={retryFetch}
                size={size}
                showCreateButton={isAdmin || isTeacher}
                createButtonText='Create Brag Log'
                onCreateClick={handleCreateBragLog}
            />
            <CreateBragLogModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <EditBragLogModal
                show={!!editingBragLog}
                bragLog={editingBragLog}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <DeleteBragLogModal
                show={!!deletingBragLog}
                bragLog={deletingBragLog}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
        </>
    );
}
