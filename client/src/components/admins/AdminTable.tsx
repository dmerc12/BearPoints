import { CreateAdminModal, EditAdminModal, DeleteAdminModal, TextFilter, BaseTable, TableColumn  } from '../index';
import { Row, Button, Col, ButtonGroup } from 'react-bootstrap';
import { formatRole, fullName } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useAppSelector } from '../../store';
import { useAdminTable } from '../../hooks';
import { useMemo, useEffect } from 'react';

interface AdminTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function AdminTable({ itemsPerPage = 10, showFilters = true, size = 'm' }: AdminTableProps) {
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const {
        data: admins, loading, error, filters, updateFilter, resetFilters,
        showCreateModal, editingItem: editingAdmin, deletingItem: deletingAdmin,
        handleCreateItem: handleCreateAdmin, handleEditItem: handleEditAdmin,
        handleDeleteItem: handleDeleteAdmin, handleCloseModals, retryFetch, handleSuccess,
    } = useAdminTable();

    useEffect(() => {
        return () => {
            resetFilters();
        };
    }, [resetFilters]);
    
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
                    <TextFilter
                        value={filters.nameSearch}
                        onChange={(value) => updateFilter('nameSearch', value)}
                        label='Search By Name'
                        placeholder='Search by name...'
                    />
                </Col>
                <Col md={6}>
                    <TextFilter
                        value={filters.emailSearch}
                        onChange={(value) => updateFilter('emailSearch', value)}
                        label='Search By Email'
                        placeholder='Search by email...'
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
                onRetry={retryFetch}
                size={size}
                showCreateButton={isAdmin}
                createButtonText='Create Administrator'
                onCreateClick={handleCreateAdmin}
            />
            <CreateAdminModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <EditAdminModal
                show={!!editingAdmin}
                admin={editingAdmin}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <DeleteAdminModal
                show={!!deletingAdmin}
                admin={deletingAdmin}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
        </>
    );
}
