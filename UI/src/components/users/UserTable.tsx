import { CreateUserModal, EditUserModal, DeleteUserModal, CrudTable } from '../index';
import { useUserTable } from '../../hooks';
import { UserDTO } from '../../services';

interface UserTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function UserTable(props: UserTableProps) {
    const { itemsPerPage = 10, showFilters = true, size = 'm' } = props;

    const {
        data, loading, error, filters, updateFilter, isAuthorized, columns,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount, sortConfig, handleSort,
    } = useUserTable({ itemsPerPage });

    return (
        <CrudTable<UserDTO>
            data={data}
            loading={loading}
            error={error}
            columns={columns}
            currentPage={currentPage}
            totalPages={totalPages}
            totalCount={totalCount}
            onPageChange={setCurrentPage}
            onRetry={retry}
            filtersConfig={showFilters ? filtersConfig : undefined}
            headerConfig={headerConfig}
            filters={filters}
            updateFilter={updateFilter}
            size={size}
            sortConfig={sortConfig}
            onSort={handleSort}
            canEdit={isAuthorized}
            canDelete={isAuthorized}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
            onCreateClick={handleCreateItem}
            createModal={
                <CreateUserModal show={showCreateModal}
                                  onCancel={handleCloseModals}
                                  onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditUserModal show={!!editingItem}
                                user={editingItem}
                                onCancel={handleCloseModals}
                                onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteUserModal show={!!deletingItem}
                                user={deletingItem}
                                onCancel={handleCloseModals}
                                onSuccess={handleSuccess}
                />
            }
        />
    );
}
