import { fullName, clearNameCaches } from '../../utils/formatNames';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { useState, useMemo, useCallback, useEffect } from 'react';
import { Row, Button, Col, ButtonGroup } from 'react-bootstrap';
import { fetchAdmins } from '../../store/slices/adminsSlice';
import { CreateAdminModal } from './CreateAdminModal';
import { DeleteAdminModal } from './DeleteAdminModal';
import BaseTable, { TableColumn } from '../BaseTable';
import { UserDTO, Role } from '../../services/types';
import { EmailFilter } from '../filters/EmailFilter';
import { formatRole } from '../../utils/formatRole';
import { NameFilter } from '../filters/NameFilter';
import { EditAdminModal } from './EditAdminModal';

interface AdminTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function AdminTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: AdminTableProps) {
    const dispatch = useAppDispatch();
    const { admins, loading, error } = useAppSelector(state => state.admins);
    const currentUser = useAppSelector(state => state.user.data);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingAdmin, setEditingAdmin] = useState<UserDTO | null>(null);
    const [deletingAdmin, setDeletingAdmin] = useState<UserDTO | null>(null);
    const [filters, setFilters] = useState({
        nameSearch: '',
        emailSearch: ''
    });

    useEffect(() => {
        clearNameCaches();
        dispatch(fetchAdmins({ page: 0, size: 1000 }));
    }, [dispatch]);
    
    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    
    const filteredAdmins = useMemo(() => {
        if(!admins.length) return [];
        const nameFilter = filters.nameSearch.toLowerCase();
        const emailFilter = filters.emailSearch.toLowerCase();
        return admins.filter((admin: UserDTO) => {
            const adminName = fullName(admin).toLowerCase();
            const adminEmail = admin.email.toLowerCase();
            const nameMatches = !nameFilter || adminName.includes(nameFilter);
            const emailMatches = !emailFilter || adminEmail.includes(emailFilter);
            return nameMatches && emailMatches;
        });
    }, [admins, filters]);
    
    const handleCreateAdmin = useCallback(() => {
        setShowCreateModal(true);
    }, []);
    
    const handleEditAdmin = useCallback((admin: UserDTO) => {
        setEditingAdmin(admin);
    }, []);
    
    const handleDeleteAdmin = useCallback((admin: UserDTO) => {
        setDeletingAdmin(admin);
    }, []);
    
    const handleCloseModals = useCallback(() => {
        setShowCreateModal(false);
        setEditingAdmin(null);
        setDeletingAdmin(null);
    }, []);
    
    const updateNameFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            nameSearch: value
        }));
    }, []);

    const updateEmailFilter = useCallback((value: string) => {
        setFilters(prev => ({
            ...prev,
            emailSearch: value
        }));
    }, []);
    
    const columns: TableColumn<UserDTO>[] = useMemo(() => [
        {
            key: 'name',
            header: 'Name',
            render: (admin: UserDTO) => fullName(admin)
        },
        {
            key: 'email',
            header: 'Email',
            render: (admin: UserDTO) => admin.email
        },
        {
            key: 'role',
            header: 'Role',
            render: (admin: UserDTO) => formatRole(admin.role)
        },
        ...(isAdmin) ? [{
            key: 'actions',
            header: 'Actions',
            render: (admin: UserDTO) => (
                <ButtonGroup size='sm'>
                    <Button variant='outline-primary'
                            onClick={() => handleEditAdmin(admin)}
                    >
                        Edit
                    </Button>
                    <Button variant='danger'
                            onClick={() => handleDeleteAdmin(admin)}
                    >
                        Delete
                    </Button>
                </ButtonGroup>
            )
        }] : []
    ], [isAdmin, handleEditAdmin, handleDeleteAdmin]);

    const renderFilters = () => {
        if (!showFilters) return null;
        return (
            <Row className='mb-3 g-3'>
                <Col md={6}>
                    <NameFilter
                        value={filters.nameSearch}
                        onChange={updateNameFilter}
                    />
                </Col>
                <Col md={6}>
                    <EmailFilter
                        value={filters.emailSearch}
                        onChange={updateEmailFilter}
                    />
                </Col>
            </Row>
        );
    };

    const renderHeader = () => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing {filteredAdmins.length} of {admins.length} administrators</span>
                {isAdmin && (
                    <Button variant='primary'
                            onClick={handleCreateAdmin}
                            className='me-2'
                            size={size === 's' ? 'sm' : undefined}
                    >
                        Create Administrator
                    </Button>
                )}
            </div>
        );
    };

    return (
        <>
            <BaseTable<UserDTO>
                data={filteredAdmins}
                loading={loading}
                error={error}
                columns={columns}
                itemsPerPage={itemsPerPage}
                renderFilters={renderFilters}
                renderHeader={renderHeader}
                onRetry={() => {
                    dispatch(fetchAdmins({ page: 0, size: 1000, force: true }));
                }}
                size={size}
            />
            <CreateAdminModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchAdmins({ page: 0, size: 1000, force: true }));
                }}
            />
            <EditAdminModal
                show={!!editingAdmin}
                admin={editingAdmin}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchAdmins({ page: 0, size: 1000, force: true }));
                }}
            />
            <DeleteAdminModal
                show={!!deletingAdmin}
                admin={deletingAdmin}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchAdmins({ page: 0, size: 1000, force: true }));
                }}
            />
        </>
    );
}
