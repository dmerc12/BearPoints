import { searchUsersInList, RootState, useAppSelector } from '../../store';
import { fullName, formatRole } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useCallback, useMemo } from 'react';
import { useTable } from '../index';

export interface UseUserTableProps {
    itemsPerPage?: number;
}

export function useUserTable({ itemsPerPage = 10 }: UseUserTableProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const isAuthorized = useMemo(() => currentUser?.role === Role.ADMIN
        || currentUser?.role === Role.STAFF, [currentUser]);

    const initialFilters = useMemo(() => ({
        firstNameSearch: '',
        lastNameSearch: '',
        emailSearch: '',
        roleFilter: '',
    }), []);

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
           firstName: filters.firstNameSearch || undefined,
           lastName: filters.lastNameSearch || undefined,
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
            key: 'firstNameSearch',
            type: 'text' as const,
            label: 'Search by first name',
            placeholder: 'Search by first name...',
        },
        {
            key: 'lastNameSearch',
            type: 'text' as const,
            label: 'Search by last name',
            placeholder: 'Search by last name...',
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
