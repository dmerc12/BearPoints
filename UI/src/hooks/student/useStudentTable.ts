import { searchStudentsInList, RootState, useAppSelector } from '../../store';
import type { FilterConfig, HeaderConfig } from '../../components';
import { fullName, formatGrade, formatName } from '../../utils';
import { StudentDTO, Role } from '../../services';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseStudentTableProps {
    itemsPerPage?: number;
}

export function useStudentTable({ itemsPerPage = 10 }: UseStudentTableProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const { data: students } = useAppSelector(state => state.students);

    const isAuthorized = useMemo(() => currentUser?.role === Role.ADMIN
        || currentUser?.role === Role.STAFF || currentUser?.role === Role.TEACHER
        || currentUser?.role === Role.PARA, [currentUser]);

    const initialFilters = useMemo(() => ({
        nameSearch: '',
        emailSearch: '',
        teacherId: '',
        minPoints: '',
        maxPoints: '',
    }), []);

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (student: StudentDTO) => fullName(student),
            sortable: true,
        },
        {
            key: 'email',
            header: 'Email',
            render: (student: StudentDTO) => student.user.email,
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (student: StudentDTO) => formatGrade(student.teacher.grade),
            sortable: true,
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (student: StudentDTO) => formatName(student.teacher) || 'N/A',
            sortable: true,
        },
        {
            key: 'points',
            header: 'Points',
            render: (student: StudentDTO) => student.points?.toString(),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        firstName?: string;
        lastName?: string;
        email?: string;
        teacherId?: number;
        minPoints?: number;
        maxPoints?: number;
    })=> {
        return searchStudentsInList(params);
    }, []);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        const nameValue = filters.nameSearch || undefined;
        const teacherId = filters.teacherId ? parseInt(filters.teacherId, 10) : undefined;
        const minPoints = filters.minPoints ? parseInt(filters.minPoints, 10) : undefined;
        const maxPoints = filters.maxPoints ? parseInt(filters.maxPoints, 10) : undefined;
        return {
            page,
            size,
            sort,
            firstName: nameValue,
            lastName: nameValue,
            email: filters.emailSearch,
            teacherId,
            minPoints,
            maxPoints,
        }
    }, []);

    const teacherOptions = useMemo(() => {
        const teacherMap = new Map<number, string>();
        students.forEach(student => {
            const teacher = student.teacher;
            if (teacher?.id) {
                if (!teacherMap.has(teacher.id)) {
                    teacherMap.set(teacher.id, formatName(teacher));
                }
            }
        });
        return Array.from(teacherMap.entries()).map(([id, name]) =>
            ({ value: id.toString(), label: name}));
    }, [students]);

    const filtersConfig: FilterConfig[] = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search by name',
            placeholder: 'First or last name...',
        },
        {
            key: 'emailSearch',
            type: 'text' as const,
            label: 'Search by email',
            placeholder: 'Email address...',
        },
        {
            key: 'teacherId',
            type: 'select' as const,
            label: 'Teacher',
            options: teacherOptions,
        },
        {
            key: 'minPoints',
            type: 'number' as const,
            label: 'Min points',
            placeholder: '0',
            min: 0,
            step: 1,
        },
        {
            key: 'maxPoints',
            type: 'number' as const,
            label: 'Max points',
            placeholder: '1000',
            min: 0,
            step: 1,
        },
    ];

    const headerConfig: HeaderConfig = useMemo(() => ({
        title: 'Students',
        itemName: 'students',
        showCreateButton: isAuthorized,
        createButtonText: 'Create Student',
        additionalElements: null,
    }), [isAuthorized]);

    const table = useTable<StudentDTO, typeof initialFilters>({
        selector: (state: RootState) => state.students,
        initialFilters,
        columnsBuilder,
        fetchAction,
        getFetchParams,
        itemsPerPage,
        mode: 'crud' as const,
    });

    const crudTable = table as typeof table & {
        showCreateModal: boolean;
        editingItem: StudentDTO | null;
        deletingItem: StudentDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: StudentDTO) => void;
        handleDeleteItem: (item: StudentDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAuthorized };
}
