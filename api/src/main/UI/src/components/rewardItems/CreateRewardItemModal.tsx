import { useAppDispatch, addRewardItem } from '../../store';
import { BaseModal, RewardItemForm } from '../index';
import { useRewardItemForm } from '../../hooks';
import { Alert } from 'react-bootstrap';
import { useEffect } from 'react';

interface CreateRewardItemModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateRewardItemModal({ show, onCancel, onSuccess }: CreateRewardItemModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading, isAdmin, handleInputChange,
        validateForm, resetForm } = useRewardItemForm({ show });

    useEffect(() => {
        if (show && !isAdmin) {
            onCancel();
            setFormErrors({ general: 'Only administrators can create reward items' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAdmin, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm()) return;
        const rewardItemData = {
            name: formData.name,
            pointCost: formData.pointCost,
            stock: formData.stock,
        };
        dispatch(addRewardItem(rewardItemData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create reward item:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Create Reward Item'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
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
