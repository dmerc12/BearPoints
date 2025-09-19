import { CreateAdminModal, EditAdminModal, DeleteAdminModal, BaseTable, ManagementButtons } from '../index';
import { useAdminTable } from '../../hooks';
import { UserDTO } from '../../services';
import { useMemo } from 'react';

export interface AdminTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function AdminTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: AdminTableProps) {
    const {
        data, loading, error, filters, updateFilter, isAdmin, baseColumns, headerConfig,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig,
        currentPage, totalPages, setCurrentPage, allData
    } = useAdminTable({ itemsPerPage });

    const columns = useMemo(() => {
        if (!isAdmin) return baseColumns;
        return [
            ...baseColumns,
            {
                key: 'actions',
                header: 'Actions',
                render: (admin: UserDTO) => (
                    <ManagementButtons
                        onEdit={() => handleEditItem(admin)}
                        onDelete={() => handleDeleteItem(admin)}
                    />
                )
            }
        ];
    }, [baseColumns, isAdmin, handleEditItem, handleDeleteItem]);

    return (
        <>
            <BaseTable<UserDTO>
                data={data}
                loading={loading}
                error={error}
                columns={columns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={allData.length}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={showFilters ? filtersConfig : undefined}
                headerConfig={headerConfig}
                filters={filters}
                updateFilter={updateFilter}
                size={size}
                onCreateClick={handleCreateItem}
            />
            <CreateAdminModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <EditAdminModal
                show={!!editingItem}
                admin={editingItem}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <DeleteAdminModal
                show={!!deletingItem}
                admin={deletingItem}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
        </>
    );
}
