import { useAppDispatch, useAppSelector, removeBehaviorType } from '../../store';
import { BehaviorTypeDTO, Role } from '../../services';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteBehaviorTypeModal {
    show: boolean;
    behaviorType: BehaviorTypeDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteBehaviorTypeModal({ show, behaviorType, onCancel, onSuccess }: DeleteBehaviorTypeModal) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.behaviorTypes);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized) {
            setAuthError('Only administrators can delete behavior types');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAuthorized]);

    const handleConfirmDelete = () => {
        if (!behaviorType || !behaviorType.id || !isAuthorized) return;
        dispatch(removeBehaviorType(behaviorType.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete behavior types', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete Behavior Type'
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
            <p>Are you sure you want to delete {behaviorType ? behaviorType.name : 'this behavior type'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {behaviorType && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Behavior Type Details:</h6>
                    <p><strong>Name:</strong> {behaviorType.name}</p>
                    <p><strong>Point Value:</strong> {behaviorType.pointValue}</p>
                    <p><strong>Status:</strong> {behaviorType.active ? 'Active' : 'Inactive'}</p>
                </div>
            )}
        </BaseModal>
    );
}
