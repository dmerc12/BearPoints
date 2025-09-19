import { CreateAdminModal, EditAdminModal, DeleteAdminModal, CrudTable } from '../index';
import { useAdminTable } from '../../hooks';
import { UserDTO } from '../../services';

export interface AdminTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function AdminTable(props: AdminTableProps) {
    const { itemsPerPage = 10, showFilters = true, size = 'm' } = props;

    const {
        data, loading, error, filters, updateFilter, isAdmin, baseColumns, headerConfig,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig,
        currentPage, totalPages, setCurrentPage, allData
    } = useAdminTable({ itemsPerPage });

    return (
        <CrudTable<UserDTO>
            data={data}
            loading={loading}
            error={error}
            columns={baseColumns}
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
            canEdit={isAdmin}
            canDelete={isAdmin}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
            onCreateClick={handleCreateItem}
            createModal={
                <CreateAdminModal show={showCreateModal}
                                  onCancel={handleCloseModals}
                                  onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditAdminModal show={!!editingItem}
                                admin={editingItem}
                                onCancel={handleCloseModals}
                                onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteAdminModal show={!!deletingItem}
                                admin={deletingItem}
                                onCancel={handleCloseModals}
                                onSuccess={handleSuccess}
                />
            }
        />
    );
}
