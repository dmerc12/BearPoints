import { fetchStudents, RootState, useAppDispatch, useAppSelector } from '../../store';
import { fullName, formatGrade, formatName, sortGrades } from '../../utils';
import { Student, Role } from '../../services';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseStudentTableProps {
    itemsPerPage?: number;
}

export function useStudentTable({ itemsPerPage = 10 }: UseStudentTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const { data: students } = useAppSelector(state => state.students);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);
    const canManageStudent = useCallback((student: Student) => {
        return isAdmin || (isTeacher && student.teacher.id === currentUser?.teacherId);
    }, [isAdmin, isTeacher, currentUser]);

    const initialFilters = { nameSearch: '', teacherFilter: '', gradeFilter: '' };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (student: Student) => fullName(student),
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (student: Student) => formatGrade(student.teacher.grade),
            sortable: true,
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (student: Student) => formatName(student.teacher) || 'N/A',
            sortable: true,
        },
        {
            key: 'points',
            header: 'Points',
            render: (student: Student) => student.points.toString(),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, force }
                                     : { page: number; size: number; force?: boolean })=> {
        dispatch(fetchStudents({ page, size, force: force || false }) as never);
    }, [dispatch]);

    const teacherNames = useMemo(() => {
        const teachers = students.map(student => student.teacher);
        return Array.from(new Set(teachers.map(t => formatName(t))));
    }, [students]);

    const grades = useMemo(() => {
        const teacherGrades = students.map(student => student.teacher.grade);
        return Array.from(new Set(teacherGrades));
    }, [students]);

    const teacherOptions = useMemo(() => {
        return teacherNames.map(teacher => ({ value: teacher, label: teacher }))
    }, [teacherNames]);

    const gradeOptions = useMemo(() => {
        const sortedGrades = sortGrades(grades);
        return sortedGrades.map(grade => ({ value: grade, label: formatGrade(grade) }));
    }, [grades]);

    const filtersConfig = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search Students',
            placeholder: 'Search by name...',
        },
        {
            key: 'teacherFilter',
            type: 'select' as const,
            label: 'Teacher',
            options: teacherOptions,
        },
        {
            key: 'gradeFilter',
            type: 'select' as const,
            label: 'Grade',
            options: gradeOptions,
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Students',
        itemName: 'students',
        showCreateButton: isAdmin || isTeacher,
        createButtonText: 'Create Student',
        additionalElements: null,
    }), [isAdmin, isTeacher]);

    const table = useTable<Student, typeof initialFilters>({
        selector: (state: RootState) => state.students,
        fetchAction,
        initialFilters,
        columnsBuilder,
        itemsPerPage,
        mode: 'crud' as const,
    });

    const crudTable = table as typeof table & {
        showCreateModal: boolean;
        editingItem: Student | null;
        deletingItem: Student | null;
        handleCreateItem: () => void;
        handleEditItem: (item: Student) => void;
        handleDeleteItem: (item: Student) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, canManageStudent };
}
