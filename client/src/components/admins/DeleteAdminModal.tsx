import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { removeAdmin } from '../../store/slices/adminsSlice';
import { Role, UserDTO } from '../../services/types';
import { formatRole } from '../../utils/formatRole';
import { fullName } from '../../utils/formatNames';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import BaseModal from '../BaseModal';

interface DeleteAdminModalProps {
    show: boolean;
    admin: UserDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteAdminModal({ show, admin, onCancel, onSuccess }: DeleteAdminModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.admins);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized) {
            setAuthError('Only administrators can delete admins');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        } else {
            setAuthError('');
        }
    }, [show, isAuthorized]);

    const handleConfirmDelete = () => {
        if (!admin || !admin.id || !isAuthorized) return;
        dispatch(removeAdmin(admin.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete admin', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete Admin'
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={handleClose}
            confirmText='Delete'
            cancelText='Cancel'
            confirmVariant='danger'
            isLoading={loading}
            disableConfirm={disableConfirm}
        >
            {authError && <Alert variant='danger'>{authError}</Alert>}
            {error && <Alert variant='danger'>{error}</Alert>}
            <p>Are you sure you want to delete {admin ? fullName(admin) : 'this admin'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {admin && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Admin Details:</h6>
                    <p><strong>Name:</strong> {fullName(admin)}</p>
                    <p><strong>Email:</strong> {admin.email}</p>
                    <p><strong>Role:</strong> {formatRole(admin.role)}</p>
                </div>
            )}
        </BaseModal>
    );
}
