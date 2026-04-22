import { searchStudentRewardsInList, RootState, useAppDispatch, useAppSelector } from '../../store';
import { StudentRewardDTO, Role } from '../../services';
import { useCallback, useEffect, useMemo } from 'react';
import { formatBragLogDate } from '../../utils';
import { useTable } from '../index';

export interface UseStudentRewardTableProps {
    itemsPerPage?: number;
}

export function useStudentRewardTable({ itemsPerPage = 10 }: UseStudentRewardTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = {
        studentName: '',
        itemName: '',
        minPointsUsed: '',
        maxPointsUsed: '',
        startDate: '',
        endDate: '',
    };

    const columnsBuilder = useCallback(() => [
        {
            key: 'student',
            header: 'Student',
            render: (reward: StudentRewardDTO) => reward.studentName || '',
            sortable: true,
        },
        {
            key: 'item',
            header: 'Reward Item',
            render: (reward: StudentRewardDTO) => reward.itemName || '',
            sortable: true,
        },
        {
            key: 'pointsUsed',
            header: 'Points Used',
            render: (reward: StudentRewardDTO) => reward.pointsUsed,
            sortable: true,
        },
        {
            key: 'date',
            header: 'Redeemed Date',
            render: (reward: StudentRewardDTO) => reward.timestamp ? formatBragLogDate(reward.timestamp) : '',
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        studentName?: string;
        itemName?: string;
        minPointsUsed?: number;
        maxPointsUsed?: number;
        startDate?: string;
        endDate?: string;
    }) => {
        dispatch(searchStudentRewardsInList(params) as never);
    }, [dispatch]);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        const studentName = filters.studentName || undefined;
        const itemName = filters.itemName || undefined;
        const minPointsUsed = filters.minPointsUsed ? parseInt(filters.minPointsUsed, 10) : undefined;
        const maxPointsUsed = filters.maxPointsUsed ? parseInt(filters.maxPointsUsed, 10) : undefined;
        const startDate = filters.startDate || undefined;
        const endDate = filters.endDate || undefined;
        return {
          page,
          size,
          sort,
          studentName,
          itemName,
          minPointsUsed,
          maxPointsUsed,
          startDate,
          endDate,
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
            key: 'itemName',
            type: 'text' as const,
            label: 'Reward Item',
            placeholder: 'Search by item name...',
        },
        {
            key: 'minPointsUsed',
            type: 'text' as const,
            label: 'Min Points Used',
            placeholder: '0',
        },
        {
            key: 'maxPointsUsed',
            type: 'text' as const,
            label: 'Max Points Used',
            placeholder: '1000',
        },
        {
            key: 'startDate',
            type: 'text' as const,
            label: 'Start Date',
            placeholder: 'YYYY-MM-DD',
        },
        {
            key: 'endDate',
            type: 'text' as const,
            label: 'End Date',
            placeholder: 'YYYY-MM-DD',
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Student Rewards',
        itemName: 'student rewards',
        showCreateButton: isAdmin,
        createButtonText: 'Create Student Reward',
        additionalElements: null,
    }), [isAdmin]);

    const table = useTable<StudentRewardDTO, typeof initialFilters>({
        selector: (state: RootState) => state.studentRewards,
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
        editingItem: StudentRewardDTO | null;
        deletingItem: StudentRewardDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: StudentRewardDTO) => void;
        handleDeleteItem: (item: StudentRewardDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    };

    return { ...crudTable, filtersConfig, headerConfig };
}
