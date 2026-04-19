import { searchStudentsInList, RootState, useAppDispatch, useAppSelector } from '../../store';
import { fullName, formatGrade, formatName } from '../../utils';
import { useCallback, useEffect, useMemo } from 'react';
import { StudentDTO, Role } from '../../services';
import { useTable } from '../index';

export interface UseStudentTableProps {
    itemsPerPage?: number;
}

export function useStudentTable({ itemsPerPage = 10 }: UseStudentTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const { data: students } = useAppSelector(state => state.students);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = {
        nameSearch: '',
        emailSearch: '',
        teacherId: '',
        minPoints: '',
        maxPoints: '',
    };

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
        dispatch(searchStudentsInList(params) as never);
    }, [dispatch]);

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

    const filtersConfig = [
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
            type: 'text' as const,
            label: 'Min points',
            placeholder: '0',
        },
        {
            key: 'maxPoints',
            type: 'text' as const,
            label: 'Max points',
            placeholder: '1000',
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Students',
        itemName: 'students',
        showCreateButton: isAdmin,
        createButtonText: 'Create Student',
        additionalElements: null,
    }), [isAdmin]);

    const table = useTable<StudentDTO, typeof initialFilters>({
        selector: (state: RootState) => state.students,
        initialFilters,
        columnsBuilder,
        fetchAction,
        getFetchParams,
        itemsPerPage,
        mode: 'crud' as const,
    });

    useEffect(() => {
        return () => {
            table.resetFilters();
        };
    }, [table]);

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

    return { ...crudTable, filtersConfig, headerConfig };
}
