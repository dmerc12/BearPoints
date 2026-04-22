import { searchRewardItemsInList, RootState, useAppDispatch, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { RewardItemDTO, Role } from '../../services';
import { useTable } from '../index';

export interface UseRewardItemTableProps {
    itemsPerPage?: number;
}

export function useRewardItemTable({ itemsPerPage = 10 }: UseRewardItemTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = {
        name: '',
        minPointCost: '',
        maxPointCost: '',
        minStock: '',
        maxStock: '',
    };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (item: RewardItemDTO) => item.name,
            sortable: true,
        },
        {
            key: 'pointCost',
            header: 'Point Cost',
            render: (item: RewardItemDTO) => item.pointCost,
            sortable: true,
        },
        {
            key: 'stock',
            header: 'Stock',
            render: (item: RewardItemDTO) => item.stock,
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        name?: string;
        minPointCost?: number;
        maxPointCost?: number;
        minStock?: number;
        maxStock?: number;
    }) => {
        dispatch(searchRewardItemsInList(params) as never);
    }, [dispatch]);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        const name = filters.name || undefined;
        const minPointCost = filters.minPointCost ? parseInt(filters.minPointCost, 10) : undefined;
        const maxPointCost = filters.maxPointCost ? parseInt(filters.maxPointCost, 10) : undefined;
        const minStock = filters.minStock ? parseInt(filters.minStock, 10) : undefined;
        const maxStock = filters.maxStock ? parseInt(filters.maxStock, 10) : undefined;
        return {
            page,
            size,
            sort,
            name,
            minPointCost,
            maxPointCost,
            minStock,
            maxStock,
        };
    }, []);

    const filtersConfig = [
        {
            key: 'name',
            type: 'text' as const,
            label: 'Name',
            placeholder: 'Search by name...',
        },
        {
            key: 'minPointCost',
            type: 'text' as const,
            label: 'Min Point Cost',
            placeholder: '0',
        },
        {
            key: 'maxPointCost',
            type: 'text' as const,
            label: 'Max Point Cost',
            placeholder: '100',
        },
        {
            key: 'minStock',
            type: 'text' as const,
            label: 'Min Stock',
            placeholder: '0',
        },
        {
            key: 'maxStock',
            type: 'text' as const,
            label: 'Max Stock',
            placeholder: '1000',
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Reward Items',
        itemName: 'reward items',
        showCreateButton: isAdmin,
        createButtonText: 'Create Reward Item',
        additionalElements: null,
    }), [isAdmin]);

    const table = useTable<RewardItemDTO, typeof initialFilters>({
        selector: (state: RootState) => state.rewardItems,
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
        editingItem: RewardItemDTO | null;
        deletingItem: RewardItemDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: RewardItemDTO) => void;
        handleDeleteItem: (item: RewardItemDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    };

    return { ...crudTable, filtersConfig, headerConfig, isAdmin };
}
