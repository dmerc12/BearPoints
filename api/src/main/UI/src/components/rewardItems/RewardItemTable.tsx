import { CreateRewardItemModal, EditRewardItemModal, DeleteRewardItemModal, CrudTable } from '../index';
import { useRewardItemTable } from '../../hooks';
import { RewardItemDTO } from '../../services';

interface RewardItemTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function RewardItemTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: RewardItemTableProps) {
    const { data, loading, error, filters, updateFilter, isAdmin, columns, sortConfig, handleSort,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount } = useRewardItemTable({ itemsPerPage });

    return (
        <CrudTable<RewardItemDTO>
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
            canEdit={isAdmin}
            canDelete={isAdmin}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
            onCreateClick={handleCreateItem}
            createModal={
                <CreateRewardItemModal
                    show={showCreateModal}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditRewardItemModal
                    show={!!editingItem}
                    rewardItem={editingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteRewardItemModal
                    show={!!deletingItem}
                    rewardItem={deletingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
        />
    );
}
