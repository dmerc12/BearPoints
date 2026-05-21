import { useAppDispatch, modifyRewardItem, fetchRewardItems } from '../../store';
import { BaseModal, RewardItemForm } from '../index';
import { useRewardItemForm } from '../../hooks';
import { RewardItemDTO} from '../../services';
import { Alert } from 'react-bootstrap';

interface EditRewardItemModalProps {
    show: boolean;
    rewardItem: RewardItemDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditRewardItemModal({ show, rewardItem, onCancel, onSuccess }: EditRewardItemModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, handleInputChange,
        validateForm, resetForm } = useRewardItemForm({ show, isEdit: true, rewardItem });

    const handleSubmit = () => {
        if (!validateForm() || !rewardItem || !rewardItem.id) return;
        const rewardItemData: RewardItemDTO = {
            name: formData.name,
            pointCost: formData.pointCost,
            stock: formData.stock,
        };
        dispatch(modifyRewardItem({ id: rewardItem.id, rewardItemData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchRewardItems({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to update reward item:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };


    return (
        <BaseModal
            title='Edit Reward Item'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <RewardItemForm
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
            />
        </BaseModal>
    );
}
