import { fullName, clearNameCaches } from '../../utils/formatNames';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { useState, useMemo, useCallback, useEffect } from 'react';
import { fetchTeachers } from '../../store/slices/teachersSlice';
import { Row, Button, Col, ButtonGroup } from 'react-bootstrap';
import { CreateTeacherModal } from './CreateTeacherModal';
import { DeleteTeacherModal } from './DeleteTeacherModal';
import { formatGrade } from '../../utils/formatGrades';
import BaseTable, { TableColumn } from '../BaseTable';
import { EditTeacherModal } from './EditTeacherModal';
import { Teacher, Role } from '../../services/types';
import { GradeFilter } from '../filters/GradeFilter';
import { NameFilter } from '../filters/NameFilter';

interface TeacherTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function TeacherTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: TeacherTableProps) {
    const dispatch = useAppDispatch();
    const { teachers, loading, error } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTeacher, setEditingTeacher] = useState<Teacher | null>(null);
    const [deletingTeacher, setDeletingTeacher] = useState<Teacher | null>(null);
    const [filters, setFilters] = useState({
        nameSearch: '',
        grade: ''
    });

    useEffect(() => {
        clearNameCaches();
        dispatch(fetchTeachers({ page: 0, size: 1000 }));
    }, [dispatch]);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);
    const canManageTeacher = useCallback((teacher: Teacher) => {
        return isAdmin || (isTeacher && teacher.user.id === currentUser?.id);
    }, [isAdmin, isTeacher, currentUser]);

    const filteredTeachers = useMemo(() => {
        if (!teachers.length) return [];
        const nameFilter = filters.nameSearch.toLowerCase();
        const gradeFilter = filters.grade;
        return teachers.filter(teacher => {
            const teacherName = fullName(teacher.user).toLowerCase();
            if (gradeFilter && teacher.grade !== gradeFilter) {
                return false;
            }
            return !(nameFilter && !teacherName.includes(nameFilter));
        });
    }, [teachers, filters])

    const grades = useMemo(() => {
        const teacherGrades = teachers.map(teacher => teacher.grade);
        return Array.from(new Set(teacherGrades));
    }, [teachers]);

    const handleCreateTeacher = useCallback(() => {
        setShowCreateModal(true);
    }, []);

    const handleEditTeacher = useCallback((teacher: Teacher) => {
        setEditingTeacher(teacher);
    }, []);

    const handleDeleteTeacher = useCallback((teacher: Teacher) => {
        setDeletingTeacher(teacher);
    }, []);

    const handleCloseModals = useCallback(() => {
        setShowCreateModal(false);
        setEditingTeacher(null);
        setDeletingTeacher(null);
    }, []);

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

    const updateNameFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            nameSearch: value
        }));
    }, []);

    const updateGradeFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            grade: value
        }));
    }, []);

    const renderFilters = () => {
        if (!showFilters) return null;
        return (
            <Row className='mb-3 g-3'>
                <Col md={6}>
                    <NameFilter
                        value={filters.nameSearch}
                        onChange={updateNameFilter}
                    />
                </Col>
                <Col md={6}>
                    <GradeFilter
                        value={filters.grade}
                        onChange={updateGradeFilter}
                        items={grades}
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
                onRetry={() =>
                    dispatch(fetchTeachers({ page: 0, size: 1000, force: true }))}
                size={size}
            />
            <CreateTeacherModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
                }}
            />
            <EditTeacherModal
                show={!!editingTeacher}
                teacher={editingTeacher}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
                }}
            />
            <DeleteTeacherModal
                show={!!deletingTeacher}
                teacher={deletingTeacher}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
                }}
            />
        </>
    );
}
