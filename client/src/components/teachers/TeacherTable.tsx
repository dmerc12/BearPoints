import {
    CreateTeacherModal, EditTeacherModal, DeleteTeacherModal,
    BaseTable, TableColumn, TextFilter, SelectFilter
} from '../index';
import { fullName, formatGrade, sortGrades } from '../../utils';
import { Row, Button, Col, ButtonGroup } from 'react-bootstrap';
import { useMemo, useCallback, useEffect } from 'react';
import { Teacher, Role } from '../../services';
import { useTeacherTable } from '../../hooks';
import { useAppSelector } from '../../store';

interface TeacherTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function TeacherTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: TeacherTableProps) {
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);
    const canManageTeacher = useCallback((teacher: Teacher) => {
        return isAdmin || (isTeacher && teacher.user.id === currentUser?.id);
    }, [isAdmin, isTeacher, currentUser]);

    const {
        data: teachers, loading, error, filters, updateFilter, resetFilters,
        showCreateModal, editingItem: editingTeacher, deletingItem: deletingTeacher,
        handleCreateItem: handleCreateTeacher, handleEditItem: handleEditTeacher,
        handleDeleteItem: handleDeleteTeacher, handleCloseModals, retryFetch, handleSuccess,
    } = useTeacherTable();

    useEffect(() => {
        return () => {
            resetFilters();
        };
    }, [resetFilters]);

    const filteredTeachers = useMemo(() => {
        if (!teachers.length) return [];
        const nameFilter = filters.nameSearch.toLowerCase();
        const gradeFilter = filters.gradeFilter;
        return teachers.filter(teacher => {
            const teacherName = fullName(teacher.user).toLowerCase();
            if (gradeFilter && teacher.grade !== gradeFilter) {
                return false;
            }
            return !(nameFilter && !teacherName.includes(nameFilter));
        });
    }, [teachers, filters]);

    const grades = useMemo(() => {
        const teacherGrades = teachers.map(teacher => teacher.grade);
        return Array.from(new Set(teacherGrades));
    }, [teachers]);

    const columns: TableColumn<Teacher>[] = useMemo(() => [
        {
            key: 'name',
            header: 'Name',
            render: (teacher: Teacher) => fullName(teacher.user)
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (teacher: Teacher) => formatGrade(teacher.grade)
        },
        {
            key: 'email',
            header: 'Email',
            render: (teacher: Teacher) => teacher.user.email
        },
        ...(isAdmin || isTeacher) ? [{
            key: 'actions',
            header: 'Actions',
            render: (teacher: Teacher) => (
                canManageTeacher(teacher) ? (
                    <ButtonGroup size='sm'>
                        <Button variant='outline-primary'
                                onClick={() => handleEditTeacher(teacher)}
                        >
                            Edit
                        </Button>
                        {isAdmin && (
                            <Button variant='danger'
                                    onClick={() => handleDeleteTeacher(teacher)}
                            >
                                Delete
                            </Button>
                        )}
                    </ButtonGroup>
                ) : null
            )
        }] : []
    ], [isAdmin, isTeacher, canManageTeacher, handleEditTeacher, handleDeleteTeacher]);

    const renderFilters = () => {
        if (!showFilters) return null;
        const sortedGrades = sortGrades(grades);
        const gradeOptions = sortedGrades.map(grade => (
            { value: grade, label: formatGrade(grade) }
        ));
        return (
            <Row className='mb-3 g-3'>
                <Col md={6}>
                    <TextFilter
                        value={filters.nameSearch}
                        onChange={(value) => updateFilter('nameSearch', value)}
                        label='Search Teachers'
                        placeholder='Search by name...'
                    />
                </Col>
                <Col md={6}>
                    <SelectFilter
                        value={filters.gradeFilter}
                        onChange={(value) => updateFilter('gradeFilter', value)}
                        label='Grade'
                        options={gradeOptions}
                    />
                </Col>
            </Row>
        );
    };

    const renderHeader = () => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing {filteredTeachers.length} of {teachers.length} teachers</span>
                {isAdmin && (
                    <Button variant='primary'
                            onClick={handleCreateTeacher}
                            className='me-2'
                            size={size === 's' ? 'sm' : undefined}
                    >
                        Create Teacher
                    </Button>
                )}
            </div>
        );
    };

    return (
        <>
            <BaseTable<Teacher>
                data={filteredTeachers}
                loading={loading}
                error={error}
                columns={columns}
                itemsPerPage={itemsPerPage}
                renderFilters={renderFilters}
                renderHeader={renderHeader}
                onRetry={retryFetch}
                size={size}
                showCreateButton={isAdmin}
                createButtonText='Create Teacher'
                onCreateClick={handleCreateTeacher}

            />
            <CreateTeacherModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <EditTeacherModal
                show={!!editingTeacher}
                teacher={editingTeacher}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <DeleteTeacherModal
                show={!!deletingTeacher}
                teacher={deletingTeacher}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
        </>
    );
}
