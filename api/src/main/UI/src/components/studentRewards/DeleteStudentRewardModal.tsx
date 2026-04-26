import { useAppDispatch, useAppSelector, removeStudentReward } from '../../store';
import { StudentRewardDTO, Role } from '../../services';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteStudentRewardModalProps {
    show: boolean;
    studentReward: StudentRewardDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteStudentRewardModal({ show, studentReward, onCancel, onSuccess }: DeleteStudentRewardModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.studentRewards);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized) {
            setAuthError('Only administrators can delete student rewards');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAuthorized]);

    const handleConfirmDelete = () => {
        if (!studentReward || !studentReward.id || !isAuthorized) return;
        dispatch(removeStudentReward(studentReward.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete student reward:', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title="Delete Reward Redemption"
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={handleClose}
            confirmText="Delete"
            cancelText="Cancel"
            confirmVariant="danger"
            isLoading={loading}
            disableConfirm={disableConfirm}
        >
            {authError && <Alert variant="danger">{authError}</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}
            <p>Are you sure you want to delete this reward redemption?</p>
            <p className="text-muted">This action cannot be undone.</p>
            {studentReward && (
                <div className="mt-3 p-3 bg-light rounded">
                    <h6>Reward Redemption Details:</h6>
                    <p><strong>Student:</strong> {studentReward.studentName || 'N/A'}</p>
                    <p><strong>Reward Item:</strong> {studentReward.itemName || 'N/A'}</p>
                    <p><strong>Points Used:</strong> {studentReward.pointsUsed || 'N/A'}</p>
                    <p><strong>Redeemed Date:</strong> {studentReward.timestamp
                        ? new Date(studentReward.timestamp).toLocaleDateString()
                        : 'N/A'}
                    </p>
                </div>
            )}
        </BaseModal>
    );
}
