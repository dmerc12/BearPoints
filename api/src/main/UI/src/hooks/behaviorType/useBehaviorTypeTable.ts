import { searchBehaviorTypesInList, RootState, useAppDispatch, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { BehaviorTypeDTO, Role } from '../../services';
import { useTable } from '../index';

export interface UseBehaviorTypeTableProps {
    itemsPerPage?: number;
}

export function useBehaviorTypeTable({ itemsPerPage = 10 }: UseBehaviorTypeTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = {
        nameSearch: '',
        statusFilter: '',
        minPointValueFilter: '',
        maxPointValueFilter: '',
    };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (behaviorType: BehaviorTypeDTO) => behaviorType.name,
            sortable: true,
        },
        {
            key: 'pointValue',
            header: 'Point Value',
            render: (behaviorType: BehaviorTypeDTO) => behaviorType.pointValue,
            sortable: true,
        },
        {
            key: 'status',
            header: 'Status',
            render: (behaviorType: BehaviorTypeDTO) => behaviorType.active ? 'Active' : 'Inactive',
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number,
        size: number;
        sort?: string;
        force?: boolean;
        name?: string;
        active?: boolean;
        minPointValue?: number;
        maxPointValue?: number;
    }) => {
        dispatch(searchBehaviorTypesInList(params) as never);
    }, [dispatch]);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        const nameValue = filters.nameSearch || undefined;
        const active = filters.statusFilter === 'true' ? true
            : filters.statusFilter === 'false' ? false
                : undefined;
        const minPointValue = filters.minPointValueFilter !== ''
            ? parseInt(filters.minPointValueFilter, 10)
            : undefined;
        const maxPointValue = filters.maxPointValueFilter !== ''
            ? parseInt(filters.maxPointValueFilter, 10)
            : undefined;
        return {
            page,
            size,
            sort,
            name: nameValue,
            active,
            minPointValue,
            maxPointValue,
        };
    }, []);

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
            key: 'minPointValueFilter',
            type: 'select' as const,
            label: 'Min Point Value',
            options: [
                { value: '', label: 'No min' },
                { value: '1', label: '1 Point' },
                { value: '2', label: '2 Points' },
                { value: '3', label: '3 Points' },
                { value: '4', label: '4 Points' },
                { value: '5', label: '5 Points' },
            ],
        },
        {
            key: 'maxPointValueFilter',
            type: 'select' as const,
            label: 'Max Point Value',
            options: [
                { value: '', label: 'No max' },
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

    const table = useTable<BehaviorTypeDTO, typeof initialFilters>({
        selector: (state: RootState) => state.behaviorTypes,
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
        editingItem: BehaviorTypeDTO | null;
        deletingItem: BehaviorTypeDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: BehaviorTypeDTO) => void;
        handleDeleteItem: (item: BehaviorTypeDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    return { ...crudTable, filtersConfig, headerConfig, isAdmin };
}
