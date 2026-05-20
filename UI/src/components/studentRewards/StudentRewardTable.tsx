import {
    CreateStudentRewardModal,
    EditStudentRewardModal,
    DeleteStudentRewardModal,
    CrudTable,
    FilterConfig, HeaderConfig
} from '../index';
import { useStudentRewardTable } from '../../hooks';
import { StudentRewardDTO } from '../../services';

interface StudentRewardTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
    customFiltersConfig?: FilterConfig[];
    customHeaderConfig?: HeaderConfig;
}

export default function StudentRewardTable({ itemsPerPage = 10, showFilters = true, size = 'm',
                                               customFiltersConfig, customHeaderConfig }: StudentRewardTableProps) {
    const {
        data, loading, error, filters, updateFilter, isAuthorized, columns, sortConfig, handleSort,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig, currentPage, totalPages,
        setCurrentPage, totalCount
    } = useStudentRewardTable({ itemsPerPage });

    const finalHeaderConfig = customHeaderConfig ? {
        ...headerConfig,
        ...customHeaderConfig,
        additionalElements: customHeaderConfig.additionalElements ?? headerConfig.additionalElements,
    } : headerConfig;

    const finalFiltersConfig = customFiltersConfig ?? (showFilters ? filtersConfig : undefined);

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
            filtersConfig={finalFiltersConfig}
            headerConfig={finalHeaderConfig}
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
