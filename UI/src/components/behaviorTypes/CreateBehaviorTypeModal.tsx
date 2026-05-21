import { useAppDispatch, addBehaviorType, fetchBehaviorTypes } from '../../store';
import { BaseModal, BehaviorTypeForm } from '../index';
import { useBehaviorTypeForm } from '../../hooks';
import { BehaviorTypeDTO } from '../../services';
import { Alert } from 'react-bootstrap';

interface CreateBehaviorTypeModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateBehaviorTypeModal({ show, onCancel, onSuccess }: CreateBehaviorTypeModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, handleInputChange, handleSelectChange,
        handleCheckboxChange, validateForm, resetForm } = useBehaviorTypeForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const behaviorTypeData: BehaviorTypeDTO = {
            name: formData.name,
            pointValue: formData.pointValue,
            active: formData.active
        };
        dispatch(addBehaviorType(behaviorTypeData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchBehaviorTypes({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to create behavior type:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    }
    
    return (
        <BaseModal
            title='Create Behavior Type'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <BehaviorTypeForm 
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
                onCheckboxChange={handleCheckboxChange}
            />
        </BaseModal>
    );
}
