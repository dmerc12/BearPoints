import { searchUsersInList, RootState, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { fullName, formatRole } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useTable } from '../index';

export interface UseUserTableProps {
    itemsPerPage?: number;
}

export function useUserTable({ itemsPerPage = 10 }: UseUserTableProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const isAuthorized = useMemo(() => currentUser?.role === Role.ADMIN
        || currentUser?.role === Role.STAFF, [currentUser]);

    const initialFilters = { nameSearch: '', emailSearch: '', roleFilter: '' };

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (user: UserDTO) => fullName(user),
            sortable: true,
        },
        {
            key: 'email',
            header: 'Email',
            render: (user: UserDTO) => user.email,
            sortable: true,
        },
        {
            key: 'role',
            header: 'Role',
            render: (user: UserDTO) => formatRole(user.role),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback((params: {
        page: number,
        size: number,
        sort?: string,
        force?: boolean,
        firstName?: string;
        lastName?: string;
        email?: string;
        role?: Role;
    }) => {
        return searchUsersInList(params);
    }, []);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string
    ) => {
        return {
           page, size, sort,
           firstName: filters.nameSearch || undefined,
           lastName: filters.nameSearch || undefined,
           email: filters.emailSearch || undefined,
           role: filters.roleFilter ? (filters.roleFilter as Role) : undefined,
        };
    }, []);

    const table = useTable<UserDTO, typeof initialFilters>({
        selector: (state: RootState) => state.users,
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
        editingItem: UserDTO | null;
        deletingItem: UserDTO | null;
        handleCreateItem: () => void;
        handleEditItem: (item: UserDTO) => void;
        handleDeleteItem: (item: UserDTO) => void;
        handleCloseModals: () => void;
        handleSuccess: () => void;
    }

    const filtersConfig = [
        {
            key: 'nameSearch',
            type: 'text' as const,
            label: 'Search by name',
            placeholder: 'Search by name...',
        },
        {
            key: 'emailSearch',
            type: 'text' as const,
            label: 'Search by email',
            placeholder: 'Search by email...',
        },
        {
            key: 'roleFilter',
            type: 'select' as const,
            label: 'Role',
            options: [
                { value: 'ADMIN', label: formatRole(Role.ADMIN) },
                { value: 'STAFF', label: formatRole(Role.STAFF) },
                { value: 'PARA', label: formatRole(Role.PARA) },
            ],
        },
    ];

    const headerConfig = useMemo(() => ({
        title: 'Users',
        itemName: 'users',
        showCreateButton: isAuthorized,
        createButtonText: 'Create User',
        additionalElements: null,
    }), [isAuthorized]);

    return { ...crudTable, filtersConfig, headerConfig, isAuthorized };
}
