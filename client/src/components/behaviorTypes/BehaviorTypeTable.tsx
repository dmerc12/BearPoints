import { formatBehaviorTypeStatus, getBehaviorTypeStatusVariant } from '../../utils/formatBehaviorType';
import { fetchBehaviorTypes } from '../../store/slices/behaviorTypesSlice';
import { Row, Button, Col, ButtonGroup, Badge } from 'react-bootstrap';
import { useBehaviorTypeTable } from '../../hooks/behaviorTypeHooks';
import { CreateBehaviorTypeModal } from './CreateBehaviorTypeModal';
import { DeleteBehaviorTypeModal } from './DeleteBehaviorTypeModal';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { EditBehaviorTypeModal } from './EditBehaviorTypeModal';
import { BehaviorType, Role } from '../../services/types';
import { useMemo, useEffect, useCallback } from 'react';
import { SelectFilter } from '../filters/SelectFilter';
import BaseTable, { TableColumn } from '../BaseTable';
import { NameFilter } from '../filters/NameFilter';

interface BehaviorTypeTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function BehaviorTypeTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: BehaviorTypeTableProps) {
    const dispatch = useAppDispatch();
    const { behaviorTypes, loading ,error } = useAppSelector(state =>
        state.behaviorTypes);
    const currentUser = useAppSelector(state =>
        state.user.data);

    const {
        filters, updateFilter, showCreateModal, editingItem: editingBehaviorType, deletingItem: deletingBehaviorType,
        handleCreateItem: handleCreateBehaviorType, handleEditItem: handleEditBehaviorType,
        handleDeleteItem: handleDeleteBehaviorType, handleCloseModals
    } = useBehaviorTypeTable();

    useEffect(() => {
        dispatch(fetchBehaviorTypes({ page: 0, size: 1000 }));
    }, [dispatch]);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const retryFetch = useCallback(() => {
        dispatch(fetchBehaviorTypes({ page: 0, size: 1000, force: true }));
    }, [dispatch]);

    const handleSuccess = useCallback(() => {
        handleCloseModals();
        retryFetch();
    }, [handleCloseModals, retryFetch]);

    const filteredBehaviorTypes = useMemo(() => {
        if (!behaviorTypes.length) return [];
        const nameFilter = filters.nameSearch.toLowerCase();
        const statusFilter = filters.statusFilter;
        const pointValueFilter = filters.pointValueFilter;
        return behaviorTypes.filter((behaviorType: BehaviorType) => {
            const behaviorTypeName = behaviorType.name.toLowerCase();
            const nameMatches = !nameFilter || behaviorTypeName.includes(nameFilter);
            const statusMatches = !statusFilter || behaviorType.active.toString() === statusFilter;
            const pointValueMatches = !pointValueFilter || behaviorType.pointValue.toString() === pointValueFilter;
            return nameMatches && statusMatches && pointValueMatches;
        });
    }, [behaviorTypes, filters]);

    const columns: TableColumn<BehaviorType>[] = useMemo(() => [
        {
            key: 'name',
            header: 'Name',
            render: (behaviorType: BehaviorType) => behaviorType.name
        },
        {
            key: 'pointValue',
            header: 'Point Value',
            render: (behaviorType: BehaviorType) => behaviorType.pointValue
        },
        {
            key: 'status',
            header: 'Status',
            render: (behaviorType: BehaviorType) => (
                <Badge bg={getBehaviorTypeStatusVariant(behaviorType.active)}>
                    {formatBehaviorTypeStatus(behaviorType.active)}
                </Badge>
            )
        },
        ...(isAdmin) ? [{
            key: 'actions',
            header: 'Actions',
            render: (behaviorType: BehaviorType) => (
                <ButtonGroup size='sm'>
                    <Button variant='outline-primary'
                            onClick={() => handleEditBehaviorType(behaviorType)}
                    >
                        Edit
                    </Button>
                    <Button variant='danger'
                            onClick={() => handleDeleteBehaviorType(behaviorType)}
                    >
                        Delete
                    </Button>
                </ButtonGroup>
            )
        }] : []
    ], [isAdmin, handleEditBehaviorType, handleDeleteBehaviorType]);

    const renderFilters = () => {
        if (!showFilters) return null;
        return (
            <Row className='mb-4 g-3'>
                <Col md={4}>
                    <NameFilter
                        value={filters.nameSearch}
                        onChange={(value) => updateFilter('nameSearch', value)}
                        label='Behavior Type Name'
                        placeholder='Search by behavior type name'
                    />
                </Col>
                <Col md={4}>
                    <SelectFilter
                        value={filters.statusFilter}
                        onChange={(value) => updateFilter('statusFilter', value)}
                        label='Status'
                        options={[
                            { value: 'true', label: 'Active' },
                            { value: 'false', label: 'Inactive' }
                        ]}
                    />
                </Col>
                <Col md={4}>
                    <SelectFilter
                        value={filters.pointValueFilter}
                        onChange={(value) => updateFilter('pointValueFilter', value)}
                        label='Point Value'
                        options={[
                            { value: '1', label: '1 Point' },
                            { value: '2', label: '2 Points' },
                            { value: '3', label: '3 Points' },
                            { value: '4', label: '4 Points' },
                            { value: '5', label: '5 Points' },
                        ]}
                    />
                </Col>
            </Row>
        );
    };

    const renderHeader = () => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing {filteredBehaviorTypes.length} of {behaviorTypes.length} behavior types</span>
                {isAdmin && (
                    <Button variant='primary'
                            onClick={handleCreateBehaviorType}
                            className='me-2'
                            size={size === 's' ? 'sm' : undefined}
                    >
                        Create Behavior Type
                    </Button>
                )}
            </div>
        );
    };

    return (
        <>
          <BaseTable<BehaviorType>
            data={filteredBehaviorTypes}
            loading={loading}
            error={error}
            columns={columns}
            itemsPerPage={itemsPerPage}
            renderFilters={renderFilters}
            renderHeader={renderHeader}
            onRetry={retryFetch}
            size={size}
            showCreateButton={isAdmin}
            createButtonText='Create Behavior Type'
            onCreateClick={handleCreateBehaviorType}
          />
          <CreateBehaviorTypeModal
              show={showCreateModal}
              onCancel={handleCloseModals}
              onSuccess={handleSuccess}
          />
          <EditBehaviorTypeModal
              show={!!editingBehaviorType}
              behaviorType={editingBehaviorType}
              onCancel={handleCloseModals}
              onSuccess={handleSuccess}
          />
          <DeleteBehaviorTypeModal
              show={!!deletingBehaviorType}
              behaviorType={deletingBehaviorType}
              onCancel={handleCloseModals}
              onSuccess={handleSuccess}
          />
        </>
    );
}
