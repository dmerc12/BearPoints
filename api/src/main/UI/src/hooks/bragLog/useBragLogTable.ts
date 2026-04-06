import { fetchBragLogs, RootState, useAppDispatch, useAppSelector } from '../../store';
import { formatBragLogDate, fullName } from '../../utils';
import { useCallback, useEffect, useMemo } from 'react';
import { BragLogDTO, Role } from '../../services';
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
            render: (bragLog: BragLogDTO) => formatBragLogDate(bragLog.timestamp),
            sortable: true,
        },
        {
            key: 'student',
            header: 'Student',
            render: (bragLog: BragLogDTO) => fullName(bragLog.student),
            sortable: true,
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (bragLog: BragLogDTO) => fullName(bragLog.teacher),
            sortable: true,
        },
        {
            key: 'behaviors',
            header: 'Behaviors',
            render: (bragLog: BragLogDTO) => bragLog.behaviors.map(b => b.name).join(', '),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, sort, force }
                                     : { page: number; size: number; sort?: string; force?: boolean }) => {
        dispatch(fetchBragLogs({ page, size, sort, force: force || false }) as never);
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

    const table = useTable<BragLogDTO, typeof initialFilters>({
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
        editingItem: BragLogDTO | null;
        deletingItem: BragLogDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: BragLogDTO) => void;
        handleDeleteItem: (item: BragLogDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin, isTeacher };
}
