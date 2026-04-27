import { CreateBragLogModal, EditBragLogModal, DeleteBragLogModal, CrudTable } from '../index';
import { getBragLogPointsVariant } from '../../utils';
import { useBragLogTable } from '../../hooks';
import { BragLogDTO } from '../../services';
import { Badge } from 'react-bootstrap';
import { useMemo } from 'react';

interface BragLogTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function BragLogTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: BragLogTableProps) {
    const {
        data, loading, error, filters, updateFilter, isAuthorized, columns, sortConfig, handleSort,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount
    } = useBragLogTable({ itemsPerPage });

    const enhancedColumns = useMemo(() => {
        const enhanced = [...columns];
        enhanced.push({
            key: 'points',
            header: 'Points',
            render: (bragLog: BragLogDTO) => (
                <Badge bg={getBragLogPointsVariant(bragLog.pointsGenerated ? bragLog.pointsGenerated : 0)}>
                    {bragLog.pointsGenerated}
                </Badge>
            ),
            sortable: true
        });
        return enhanced;
    }, [columns]);

    return (
        <CrudTable<BragLogDTO>
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
            sortConfig={sortConfig}
            onSort={handleSort}
            canEdit={isAuthorized}
            canDelete={isAuthorized}
            onEditItem={handleEditItem}
            onDeleteItem={handleDeleteItem}
            onCreateClick={handleCreateItem}
            createModal={
                <CreateBragLogModal
                    show={showCreateModal}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            editModal={
                <EditBragLogModal
                    show={!!editingItem}
                    bragLog={editingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
            deleteModal={
                <DeleteBragLogModal
                    show={!!deletingItem}
                    bragLog={deletingItem}
                    onCancel={handleCloseModals}
                    onSuccess={handleSuccess}
                />
            }
        />
    );
}
