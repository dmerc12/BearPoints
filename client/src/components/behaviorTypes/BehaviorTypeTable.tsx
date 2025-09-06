import { formatBehaviorTypeStatus, getBehaviorTypeStatusVariant } from '../../utils/formatBehaviorType';
import { fetchBehaviorTypes } from '../../store/slices/behaviorTypesSlice';
import { Row, Button, Col, ButtonGroup, Badge } from 'react-bootstrap';
import { CreateBehaviorTypeModal } from './CreateBehaviorTypeModal';
import { DeleteBehaviorTypeModal } from './DeleteBehaviorTypeModal';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { EditBehaviorTypeModal } from './EditBehaviorTypeModal';
import { useState, useMemo, useCallback, useEffect } from 'react';
import { PointValueFilter} from '../filters/PointValueFilter';
import { BehaviorType, Role } from '../../services/types'
import { StatusFilter } from '../filters/StatusFilter';
import BaseTable, { TableColumn } from '../BaseTable';
import { NameFilter } from '../filters/NameFilter';

interface BehaviorTypeTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function BehaviorTypeTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: BehaviorTypeTableProps) {
    const dispatch = useAppDispatch();
    const { behaviorTypes, loading ,error } = useAppSelector(state => state.behaviorTypes);
    const currentUser = useAppSelector(state => state.user.data);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingBehaviorType, setEditingBehaviorType] = useState<BehaviorType | null>(null);
    const [deletingBehaviorType, setDeletingBehaviorType] = useState<BehaviorType | null>(null);
    const [filters, setFilters] = useState({
        nameSearch: '',
        statusFilter: '',
        pointValueFilter: '',
    });

    useEffect(() => {
        dispatch(fetchBehaviorTypes({ page: 0, size: 1000 }));
    }, [dispatch]);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

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

    const handleCreateBehaviorType = useCallback(()=> {
        setShowCreateModal(true);
    }, []);

    const handleEditBehaviorType = useCallback((behaviorType: BehaviorType)=> {
        setEditingBehaviorType(behaviorType);
    }, []);

    const handleDeleteBehaviorType = useCallback((behaviorType: BehaviorType)=> {
        setDeletingBehaviorType(behaviorType);
    }, []);

    const handleCloseModals = useCallback(() => {
        setShowCreateModal(false);
        setEditingBehaviorType(null);
        setDeletingBehaviorType(null);
    }, []);

    const updateNameFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            nameSearch: value
        }));
    }, []);

    const updateStatusFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            statusFilter: value
        }));
    }, []);

    const updatePointValueFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            pointValueFilter: value
        }));
    }, []);

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
                        onChange={updateNameFilter}
                        label='Behavior Type Name'
                        placeholder='Search by behavior type name'
                    />
                </Col>
                <Col md={4}>
                    <StatusFilter
                        value={filters.statusFilter}
                        onChange={updateStatusFilter}
                    />
                </Col>
                <Col md={4}>
                    <PointValueFilter
                        value={filters.pointValueFilter}
                        onChange={updatePointValueFilter}
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
            onRetry={() => {
                dispatch(fetchBehaviorTypes({ page: 0, size: 1000, force: true }));
            }}
            size={size}
          />
          <CreateBehaviorTypeModal
              show={showCreateModal}
              onCancel={handleCloseModals}
              onSuccess={() => {
                handleCloseModals();
                dispatch(fetchBehaviorTypes({ page: 0, size: 1000, force: true }));
              }}
          />
          <EditBehaviorTypeModal
              show={!!editingBehaviorType}
              behaviorType={editingBehaviorType}
              onCancel={handleCloseModals}
              onSuccess={() => {
                handleCloseModals();
                dispatch(fetchBehaviorTypes({ page: 0, size: 1000, force: true }));
              }}
          />
          <DeleteBehaviorTypeModal
              show={!!deletingBehaviorType}
              behaviorType={deletingBehaviorType}
              onCancel={handleCloseModals}
              onSuccess={() => {
                  handleCloseModals();
                  dispatch(fetchBehaviorTypes({ page: 0, size: 1000, force: true }));
              }}
          />
        </>
    );
}
