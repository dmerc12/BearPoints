import { CreateStudentRewardModal, EditStudentRewardModal, DeleteStudentRewardModal, CrudTable } from '../index';
import { useStudentRewardTable } from '../../hooks';
import { StudentRewardDTO } from '../../services';

interface StudentRewardTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function StudentRewardTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: StudentRewardTableProps) {
    const {
        data, loading, error, filters, updateFilter, isAdmin, columns, sortConfig, handleSort,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig, currentPage, totalPages,
        setCurrentPage, totalCount
    } = useStudentRewardTable({ itemsPerPage });

    return (
        <CrudTable<StudentRewardDTO>
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
                <CreateStudentRewardModal
                    show={showCreateModal}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditStudentRewardModal
                    show={!!editingItem}
                    studentReward={editingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteStudentRewardModal
                    show={!!deletingItem}
                    studentReward={deletingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
        />
    );
}
