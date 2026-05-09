import { searchStudentRewardsInList, RootState, useAppSelector } from '../../store';
import { FilterConfig, HeaderConfig } from '../../components';
import { StudentRewardDTO, Role } from '../../services';
import { formatBragLogDate } from '../../utils';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseStudentRewardTableProps {
    itemsPerPage?: number;
}

export function useStudentRewardTable({ itemsPerPage = 10 }: UseStudentRewardTableProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const isAuthorized = useMemo(() => currentUser?.role === Role.ADMIN
        || currentUser?.role === Role.STAFF || currentUser?.role === Role.TEACHER
        || currentUser?.role === Role.PARA, [currentUser]);

    const initialFilters = useMemo(() => ({
        studentName: '',
        itemName: '',
        minPointsUsed: '',
        maxPointsUsed: '',
        startDate: '',
        endDate: '',
    }), []);

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
        return  searchStudentRewardsInList(params);
    }, []);

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

    const filtersConfig: FilterConfig[] = [
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
            type: 'number' as const,
            label: 'Min Points Used',
            placeholder: '0',
            min: 0,
            step: 1,
        },
        {
            key: 'maxPointsUsed',
            type: 'number' as const,
            label: 'Max Points Used',
            placeholder: '1000',
            min: 0,
            step: 1,
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

    const headerConfig: HeaderConfig = useMemo(() => ({
        title: 'Reward Redemptions',
        itemName: 'student rewards',
        showCreateButton: isAuthorized,
        createButtonText: 'Redeem Rewards',
        additionalElements: null,
    }), [isAuthorized]);

    const table = useTable<StudentRewardDTO, typeof initialFilters>({
        selector: (state: RootState) => state.studentRewards,
        initialFilters,
        columnsBuilder,
        fetchAction,
        getFetchParams,
        itemsPerPage,
        mode: 'crud' as const,
    });

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

    return { ...crudTable, filtersConfig, headerConfig, isAuthorized };
}
