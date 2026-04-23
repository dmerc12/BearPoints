import { useAppDispatch, useAppSelector, removeUser } from '../../store';
import { formatRole, fullName } from '../../utils';
import { Role, UserDTO } from '../../services';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteUserModalProps {
    show: boolean;
    user: UserDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteUserModal({ show, user, onCancel, onSuccess }: DeleteUserModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.users);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized) {
            setAuthError('Only administrators can delete users');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        } else {
            setAuthError('');
        }
    }, [show, isAuthorized]);

    const handleConfirmDelete = () => {
        if (!user || !user.id || !isAuthorized) return;
        dispatch(removeUser(user.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete user', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete User'
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
            <p>Are you sure you want to delete {user ? fullName(user) : 'this user'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {user && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>User Details:</h6>
                    <p><strong>Name:</strong> {fullName(user)}</p>
                    <p><strong>Email:</strong> {user.email}</p>
                    <p><strong>Role:</strong> {formatRole(user.role)}</p>
                </div>
            )}
        </BaseModal>
    );
}
