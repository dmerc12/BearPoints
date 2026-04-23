import { searchBragLogsInList, RootState, useAppDispatch, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { BragLogDTO, Role } from '../../services';
import { formatBragLogDate } from '../../utils';
import { useTable } from '../index';

export interface UseBragLogTableProps {
    itemsPerPage?: number;
}

export function useBragLogTable({ itemsPerPage = 10 }: UseBragLogTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isStaff = useMemo(() => currentUser?.role === Role.STAFF, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);

    const initialFilters = {
        studentName: '',
        teacherName: '',
        minPoints: '',
        maxPoints: '',
        startDate: '',
        endDate: '',
        submitterName: '',
    };

    const columnsBuilder = useCallback(() => [
        {
            key: 'date',
            header: 'Date',
            render: (bragLog: BragLogDTO) => formatBragLogDate(bragLog.timestamp!),
            sortable: true,
        },
        {
            key: 'student',
            header: 'Student',
            render: (bragLog: BragLogDTO) => bragLog.studentName || '',
            sortable: true,
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (bragLog: BragLogDTO) => bragLog.teacherName || '',
            sortable: true,
        },
        {
            key: 'behaviors',
            header: 'Behaviors',
            render: (bragLog: BragLogDTO) => bragLog.behaviors?.map(b => b.name).join(', ') || '',
            sortable: true,
        },
        {
            key: 'points',
            header: 'Points',
            render: (bragLog: BragLogDTO) => bragLog.pointsGenerated,
            sortable: true,
        },
        {
            key: 'submitter',
            header: 'Submitted By',
            render: (bragLog: BragLogDTO) => bragLog.submitterName,
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        studentName?: string;
        teacherName?: string;
        minPoints?: number;
        maxPoints?: number;
        startDate?: string;
        endDate?: string;
        submitterName?: string;
    }) => {
        dispatch(searchBragLogsInList(params) as never);
    }, [dispatch]);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        const studentName = filters.studentName || undefined;
        const teacherName = filters.teacherName || undefined;
        const minPoints = filters.minPoints
            ? parseInt(filters.minPoints, 10)
            : undefined;
        const maxPoints = filters.maxPoints
            ? parseInt(filters.maxPoints, 10)
            : undefined;
        const startDate = filters.startDate || undefined;
        const endDate = filters.endDate || undefined;
        const submitterName = filters.submitterName || undefined;
        return {
            page,
            size,
            sort,
            studentName,
            teacherName,
            minPoints,
            maxPoints,
            startDate,
            endDate,
            submitterName,
        };
    }, []);

    const filtersConfig = [
        {
            key: 'studentName',
            type: 'text' as const,
            label: 'Student Name',
            placeholder: 'Search by student name...',
        },
        {
            key: 'teacherName',
            type: 'text' as const,
            label: 'Teacher Name',
            placeholder: 'Search by teacher name...',
        },
        {
            key: 'submitterName',
            type: 'text' as const,
            label: 'Submitted By',
            placeholder: 'Search by submitter name...',
        },
        {
            key: 'minPoints',
            type: 'text' as const,
            label: 'Min Points',
            placeholder: '0',
        },
        {
            key: 'maxPoints',
            type: 'text' as const,
            label: 'Max Points',
            placeholder: '1000',
        },
        {
            key: 'startDate',
            type: 'date' as const,
            label: 'Start Date',
            placeholder: 'MMM dd, yyyy',
        },
        {
            key: 'endDate',
            type: 'date' as const,
            label: 'End Date',
            placeholder: 'MMM dd, yyyy',
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Brag Logs',
        itemName: 'brag logs',
        showCreateButton: isAdmin || isStaff || isTeacher,
        createButtonText: 'Create Brag Log',
        additionalElements: null,
    }), [isAdmin, isStaff, isTeacher]);

    const table = useTable<BragLogDTO, typeof initialFilters>({
        selector: (state: RootState) => state.bragLogs,
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
        editingItem: BragLogDTO | null;
        deletingItem: BragLogDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: BragLogDTO) => void;
        handleDeleteItem: (item: BragLogDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin, isStaff, isTeacher };
}
