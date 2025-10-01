import { fetchBehaviorTypes, RootState, useAppDispatch, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { BehaviorType, Role } from '../../services';
import { useTable } from '../index';

export interface UseBehaviorTypeTableProps {
    itemsPerPage?: number;
}

export function useBehaviorTypeTable({ itemsPerPage }: UseBehaviorTypeTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = { nameSearch: '', statusFilter: '', pointValueFilter: '' };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (behaviorType: BehaviorType) => behaviorType.name,
            sortable: true,
        },
        {
            key: 'pointValue',
            header: 'Point Value',
            render: (behaviorType: BehaviorType) => behaviorType.pointValue,
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, sort, force }
                                     : { page: number, size: number; sort?: string; force?: boolean }) => {
        dispatch(fetchBehaviorTypes({ page, size, sort, force: force || false }) as never);
    }, [dispatch]);

    const filtersConfig = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search Behavior Types',
            placeholder: 'Search by name...',
        },
        {
            key: 'statusFilter',
            type: 'select' as const,
            label: 'Status',
            options: [
                { value: 'true', label: 'Active' },
                { value: 'false', label: 'Inactive' },
            ],
        },
        {
            key: 'pointValueFilter',
            type: 'select' as const,
            label: 'Point Value',
            options: [
                { value: '1', label: '1 Point' },
                { value: '2', label: '2 Points' },
                { value: '3', label: '3 Points' },
                { value: '4', label: '4 Points' },
                { value: '5', label: '5 Points' },
            ],
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Behavior Types',
        itemName: 'behavior types',
        showCreateButton: isAdmin,
        createButtonText: 'Create Behavior Type',
        additionalElements: null,
    }), [isAdmin]);

    const table = useTable<BehaviorType, typeof initialFilters>({
        selector: (state: RootState) => state.behaviorTypes,
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
        editingItem: BehaviorType | null;
        deletingItem: BehaviorType | null;
        handleCreateItem: () => void;
        handleEditItem: (item: BehaviorType) => void;
        handleDeleteItem: (item: BehaviorType) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin };
}
