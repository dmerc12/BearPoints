import { fetchTeachers, RootState, useAppSelector, useAppDispatch } from '../../store';
import { fullName, formatGrade, sortGrades } from '../../utils';
import { Teacher, Role } from '../../services';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseTeacherTableProps {
    itemsPerPage?: number;
}

export function useTeacherTable({ itemsPerPage = 10 }: UseTeacherTableProps) {
    const dispatch = useAppDispatch();
    const { data: teachers } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);
    const canManageTeacher = useCallback((teacher: Teacher) => {
        return isAdmin || (isTeacher && teacher.user.id === currentUser?.id);
    }, [isAdmin, isTeacher, currentUser]);

    const initialFilters = { nameSearch: '', gradeFilter: '' };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (teacher: Teacher) => fullName(teacher.user),
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (teacher: Teacher) => formatGrade(teacher.grade),
            sortable: true,
        },
        {
            key: 'email',
            header: 'Email',
            render: (teacher: Teacher) => teacher.user.email,
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, force }
                                     : { page: number; size: number; force?: boolean }) => {
        dispatch(fetchTeachers({ page, size, force: force || false }) as never);
    }, [dispatch]);

    const gradeOptions = useMemo(() => {
        const teacherGrades = teachers.map(teacher => teacher.grade);
        const uniqueGrades = Array.from(new Set(teacherGrades));
        const sortedGrades = sortGrades(uniqueGrades);
        return sortedGrades.map(grade => ({ value: grade, label: formatGrade(grade) }));
    }, [teachers]);

    const filtersConfig = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search Teachers',
            placeholder: 'Search by name...',
        },
        {
            key: 'gradeFilter',
            type: 'select' as const,
            label: 'Grade',
            options: gradeOptions,
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Teachers',
        itemName: 'teachers',
        showCreateButton: isAdmin,
        createButtonText: 'Create Teacher',
        additionalElements: null,
    }), [isAdmin]);

    const table = useTable<Teacher, typeof initialFilters>({
        selector: (state: RootState) => state.teachers,
        fetchAction,
        initialFilters,
        columnsBuilder,
        itemsPerPage,
        mode: 'crud' as const,
    });

    const crudTable = table as typeof table & {
        showCreateModal: boolean;
        editingItem: Teacher | null;
        deletingItem: Teacher | null;
        handleCreateItem: () => void;
        handleEditItem: (item: Teacher) => void;
        handleDeleteItem: (item: Teacher) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin, canManageTeacher };
}
