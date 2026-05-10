import { searchTeachersInList, RootState, useAppSelector } from '../../store';
import { fullName, formatGrade, sortGrades } from '../../utils';
import { TeacherDTO, Role, GradeLevel } from '../../services';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseTeacherTableProps {
    itemsPerPage?: number;
}

export function useTeacherTable({ itemsPerPage = 10 }: UseTeacherTableProps) {
    const currentUser = useAppSelector(state => state.user.data);

    const isAuthorized = useMemo(() => currentUser?.role === Role.ADMIN
        || currentUser?.role === Role.STAFF, [currentUser]);

    const initialFilters = useMemo(() => ({
        nameSearch: '',
        emailSearch: '',
        gradeFilter: ''
    }), []);

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (teacher: TeacherDTO) => fullName(teacher.user),
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (teacher: TeacherDTO) => formatGrade(teacher.grade),
            sortable: true,
        },
        {
            key: 'email',
            header: 'Email',
            render: (teacher: TeacherDTO) => teacher.user.email,
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string
        force?: boolean;
        firstName?: string;
        lastName?: string;
        email?: string;
        grade?: GradeLevel;
    }) => {
        return searchTeachersInList(params);
    }, []);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        return {
            page,
            size,
            sort,
            firstName: filters.nameSearch || undefined,
            lastName: filters.nameSearch || undefined,
            email: filters.emailSearch || undefined,
            grade: filters.gradeFilter ? (filters.gradeFilter as GradeLevel) : undefined,
        };
    }, []);

    const gradeOptions = useMemo(() => {
        const allGrades = Object.values(GradeLevel);
        const sortedGrades = sortGrades(allGrades);
        return sortedGrades.map(grade => ({ value: grade, label: formatGrade(grade) }));
    }, []);

    const filtersConfig = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search by name',
            placeholder: 'Search by name...',
        },
        {
            key: 'emailSearch',
            type: 'text' as const,
            label: 'Search by email',
            placeholder: 'Search by email...',
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
        showCreateButton: isAuthorized,
        createButtonText: 'Create Teacher',
        additionalElements: null,
    }), [isAuthorized]);

    const table = useTable<TeacherDTO, typeof initialFilters>({
        selector: (state: RootState) => state.teachers,
        initialFilters,
        columnsBuilder,
        fetchAction,
        getFetchParams,
        itemsPerPage,
        mode: 'crud' as const,
    });

    const crudTable = table as typeof table & {
        showCreateModal: boolean;
        editingItem: TeacherDTO | null;
        deletingItem: TeacherDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: TeacherDTO) => void;
        handleDeleteItem: (item: TeacherDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAuthorized };
}
