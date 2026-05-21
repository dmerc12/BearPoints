import { useAppDispatch, useAppSelector, removeStudentReward, fetchStudentRewards } from '../../store';
import { StudentRewardDTO } from '../../services';
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

    const handleConfirmDelete = () => {
        if (!studentReward || !studentReward.id) return;
        dispatch(removeStudentReward(studentReward.id))
            .unwrap()
            .then(() => {
                onSuccess();
                dispatch(fetchStudentRewards({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to delete student reward:', error);
            });
    };

    return (
        <BaseModal
            title="Delete Reward Redemption"
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={onCancel}
            confirmText="Delete"
            cancelText="Cancel"
            confirmVariant="danger"
            isLoading={loading}
            disableConfirm={loading}
        >
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
