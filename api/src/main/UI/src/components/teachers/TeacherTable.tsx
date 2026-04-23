import { CreateTeacherModal, EditTeacherModal, DeleteTeacherModal, CrudTable } from '../index';
import { useTeacherTable } from '../../hooks';
import { TeacherDTO } from '../../services';

interface TeacherTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function TeacherTable(props: TeacherTableProps) {
    const { itemsPerPage = 10, showFilters = true, size = 'm' } = props;

    const {
        data, loading, error, filters, updateFilter, isAdmin, columns, sortConfig, handleSort,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount
    } = useTeacherTable({ itemsPerPage });

    return (
        <CrudTable<TeacherDTO>
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
                <CreateTeacherModal
                    show={showCreateModal}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditTeacherModal
                    show={!!editingItem}
                    teacher={editingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteTeacherModal
                    show={!!deletingItem}
                    teacher={deletingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
        />
    );
}
