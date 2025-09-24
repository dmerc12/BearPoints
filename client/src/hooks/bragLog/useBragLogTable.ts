import { fetchBragLogs, RootState, useAppDispatch, useAppSelector } from '../../store';
import { formatBragLogDate, fullName } from '../../utils';
import { useCallback, useEffect, useMemo } from 'react';
import { BragLog, Role } from '../../services';
import { useTable } from '../index';

export interface UseBragLogTableProps {
    itemsPerPage?: number;
}

export function useBragLogTable({ itemsPerPage = 10 }: UseBragLogTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);

    const initialFilters = { studentFilter: '', teacherFilter: '', dateFilter: '' };

    const columnsBuilder = useCallback(() => [
        {
            key: 'date',
            header: 'Date',
            render: (bragLog: BragLog) => formatBragLogDate(bragLog.timestamp),
            sortable: true,
        },
        {
            key: 'student',
            header: 'Student',
            render: (bragLog: BragLog) => fullName(bragLog.student),
            sortable: true,
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (bragLog: BragLog) => fullName(bragLog.teacher),
            sortable: true,
        },
        {
            key: 'behaviors',
            header: 'Behaviors',
            render: (bragLog: BragLog) => bragLog.behaviors.map(b => b.name).join(', '),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, force }
                                     : { page: number; size: number; force?: boolean }) => {
        dispatch(fetchBragLogs({ page, size, force: force || false }) as never);
    }, [dispatch]);

    const filtersConfig = [
        {
            key: 'studentFilter',
            type: 'text' as const,
            label: 'Search Students',
            placeholder: 'Search by student name...',
        },
        {
            key: 'teacherFilter',
            type: 'text' as const,
            label: 'Search Teachers',
            placeholder: 'Search by teacher name...',
        },
        {
            key: 'dateFilter',
            type: 'text' as const,
            label: 'Search by Date',
            placeholder: 'MM/DD/YYYY',
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Brag Logs',
        itemName: 'brag logs',
        showCreateButton: isAdmin || isTeacher,
        createButtonText: 'Create Brag Log',
        additionalElements: null,
    }), [isAdmin, isTeacher]);

    const table = useTable<BragLog, typeof initialFilters>({
        selector: (state: RootState) => state.bragLogs,
        fetchAction,
        initialFilters,
        columnsBuilder,
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
        editingItem: BragLog | null;
        deletingItem: BragLog | null;
        handleCreateItem: () => void;
        handleEditItem: (item: BragLog) => void;
        handleDeleteItem: (item: BragLog) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin, isTeacher };
}
