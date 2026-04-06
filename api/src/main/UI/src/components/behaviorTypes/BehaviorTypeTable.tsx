import { CreateBehaviorTypeModal, EditBehaviorTypeModal, DeleteBehaviorTypeModal, CrudTable } from '../index';
import { formatBehaviorTypeStatus, getBehaviorTypeStatusVariant } from '../../utils';
import { useBehaviorTypeTable } from '../../hooks';
import { BehaviorTypeDTO } from '../../services';
import { Badge } from 'react-bootstrap';
import { useMemo } from 'react';

interface BehaviorTypeTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function BehaviorTypeTable(props: BehaviorTypeTableProps) {
    const { itemsPerPage = 10, showFilters = true, size = 'm' } = props;

    const {
        data, loading, error, filters, updateFilter, isAdmin, columns,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount
    } = useBehaviorTypeTable({ itemsPerPage });

    const enhancedColumns = useMemo(() => {
        const enhanced = [...columns];
        enhanced.push({
            key: 'status',
            header: 'Status',
            render: (behaviorType: BehaviorTypeDTO) => (
                <Badge bg={getBehaviorTypeStatusVariant(behaviorType.active)}>
                    {formatBehaviorTypeStatus(behaviorType.active)}
                </Badge>
            ),
            sortable: true
        });
        return enhanced;
    }, [columns]);

    return (
          <CrudTable<BehaviorTypeDTO>
            data={data}
            loading={loading}
            error={error}
            columns={enhancedColumns}
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
            canEdit={isAdmin}
            canDelete={isAdmin}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
            onCreateClick={handleCreateItem}
            createModal={
                <CreateBehaviorTypeModal
                    show={showCreateModal}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditBehaviorTypeModal
                    show={!!editingItem}
                    behaviorType={editingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteBehaviorTypeModal
                    show={!!deletingItem}
                    behaviorType={deletingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
          />
    );
}
