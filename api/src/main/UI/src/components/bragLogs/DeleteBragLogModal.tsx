import { useAppDispatch, useAppSelector, removeBragLog } from '../../store';
import { BragLogDTO, Role } from '../../services';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteBragLogModalProps {
    show: boolean;
    bragLog: BragLogDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteBragLogModal({ show, bragLog, onCancel, onSuccess }: DeleteBragLogModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.bragLogs);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized) {
            setAuthError('Only administrators can delete brag logs');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAuthorized]);

    const handleConfirmDelete = () => {
        if (!bragLog || !bragLog.id || !isAuthorized) return;
        dispatch(removeBragLog(bragLog.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete brag log', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete Brag Log'
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
            <p>Are you sure you want to delete this brag log?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {bragLog && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Brag Log Details:</h6>
                    <p><strong>Student:</strong> {bragLog.studentName}</p>
                    <p><strong>Teacher:</strong> {bragLog.teacherName}</p>
                    <p><strong>Behaviors:</strong> {bragLog.behaviors?.map(b => b.name)
                        .join(', ')}</p>
                    <p><strong>Points Generated:</strong> {bragLog.pointsGenerated}</p>
                    <p><strong>Date:</strong> {bragLog.timestamp
                        ? new Date(bragLog.timestamp).toLocaleDateString()
                        : 'N/A'}</p>
                    {bragLog.notes && (
                        <p><strong>Notes:</strong> {bragLog.notes}</p>
                    )}
                </div>
            )}
        </BaseModal>
    );
}
