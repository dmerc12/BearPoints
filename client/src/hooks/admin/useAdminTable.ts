import { fetchAdmins, RootState, useAppDispatch, useAppSelector } from '../../store';
import { useCallback, useEffect, useMemo } from 'react';
import { fullName, formatRole } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useTable } from '../index';

export interface UseAdminTableProps {
    itemsPerPage?: number;
}

export function useAdminTable({ itemsPerPage = 10 }: UseAdminTableProps) {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialFilters = { nameSearch: '', emailSearch: '' };

    const filterFunction = useCallback((data: UserDTO[], filters: { nameSearch: string, emailSearch: string }) => {
        return data.filter((admin: UserDTO) => {
            const nameFilter = filters.nameSearch.toLowerCase();
            const emailFilter = filters.emailSearch.toLowerCase();
            const adminName = fullName(admin).toLowerCase();
            const adminEmail = admin.email.toLowerCase();
            const nameMatches = !nameFilter || adminName.includes(nameFilter);
            const emailMatches = !emailFilter || adminEmail.includes(emailFilter);
            return nameMatches && emailMatches;
        });
    }, []);

    const columnsBuilder = useCallback(() => [
        {
            key: 'name',
            header: 'Name',
            render: (admin: UserDTO) => fullName(admin),
            sortable: true,
        },
        {
            key: 'email',
            header: 'Email',
            render: (admin: UserDTO) => admin.email,
            sortable: true,
        },
        {
            key: 'role',
            header: 'Role',
            render: (admin: UserDTO) => formatRole(admin.role),
            sortable: true,
        },
    ], []);

    const fetchAction = useCallback(({ page, size, force }: { page: number; size: number; force?: boolean }) => {
        dispatch(fetchAdmins({ page, size, force: force || false }) as never);
    }, [dispatch]);

    const table = useTable({
        selector: (state: RootState) => ({
            data: state.admins.data,
            loading: state.admins.loading,
            error: state.admins.error
        }),
        initialFilters,
        filterFunction,
        columnsBuilder,
        fetchAction,
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
    ];

    const headerConfig = useMemo(() => ({
        title: 'Administrators',
        itemName: 'administrators',
        showCreateButton: isAdmin,
        createButtonText: 'Create Administrator',
        additionalElements: null,
    }), [isAdmin]);

    useEffect(() => {
        return () => {
            crudTable.resetFilters();
        };
    }, [crudTable]);

    return { ...crudTable, filtersConfig, headerConfig, isAdmin, baseColumns: table.columns };
}
