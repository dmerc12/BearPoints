import { useAppDispatch, modifyRewardItem } from '../../store';
import { BaseModal, RewardItemForm } from '../index';
import { useRewardItemForm } from '../../hooks';
import { RewardItemDTO} from '../../services';
import { Alert } from 'react-bootstrap';
import { useEffect } from 'react';

interface EditRewardItemModalProps {
    show: boolean;
    rewardItem: RewardItemDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditRewardItemModal({ show, rewardItem, onCancel, onSuccess }: EditRewardItemModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading, isAdmin, handleInputChange,
        validateForm, resetForm } = useRewardItemForm({ show, isEdit: true, rewardItem });

    useEffect(() => {
        if (show && !isAdmin) {
            onCancel();
            setFormErrors({ general: 'Only administrators can edit reward items' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 300);
            return () => clearTimeout(timer);
        }
    }, [show, isAdmin, onCancel, setFormErrors]);

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
