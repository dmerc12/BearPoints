import { addBehaviorType } from '../../store/slices/behaviorTypesSlice';
import { useBehaviorTypeForm } from '../../hooks/useBehaviorTypeForm';
import { BehaviorTypeForm } from './BehaviorTypeForm';
import { useAppDispatch } from '../../store/hooks';
import { Alert } from 'react-bootstrap';
import BaseModal from '../BaseModal';

interface CreateBehaviorTypeModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateBehaviorTypeModal({ show, onCancel, onSuccess }: CreateBehaviorTypeModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, handleInputChange, handleCheckboxChange,
        validateForm, resetForm } = useBehaviorTypeForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const behaviorTypeData = {
            name: formData.name,
            pointValue: formData.pointValue,
            active: formData.active
        };
        dispatch(addBehaviorType(behaviorTypeData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
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
                onCheckboxChange={handleCheckboxChange}
            />
        </BaseModal>
    );
}
