import { useAppDispatch, useAppSelector, removeRewardItem } from '../../store';
import { RewardItemDTO } from '../../services';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteRewardItemModalProps {
    show: boolean;
    rewardItem: RewardItemDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteRewardItemModal({ show, rewardItem, onCancel, onSuccess }: DeleteRewardItemModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector((state) => state.rewardItems);

    const handleConfirmDelete = () => {
        if (!rewardItem || !rewardItem.id) return;
        dispatch(removeRewardItem(rewardItem.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete reward item:', error);
            });
    };

    return (
        <BaseModal
            title='Delete Reward Item'
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={onCancel}
            confirmText='Delete'
            cancelText='Cancel'
            confirmVariant='danger'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <p>Are you sure you want to delete {rewardItem ? rewardItem.name : 'this reward item'}?</p>
            <p className="text-muted">This action cannot be undone.</p>
            {rewardItem && (
                <div className="mt-3 p-3 bg-light rounded">
                    <h6>Reward Item Details:</h6>
                    <p><strong>Name:</strong> {rewardItem.name}</p>
                    <p><strong>Point Cost:</strong> {rewardItem.pointCost}</p>
                    <p><strong>Stock:</strong> {rewardItem.stock}</p>
                </div>
            )}
        </BaseModal>
    );
}
