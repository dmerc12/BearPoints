import { modifyBehaviorType } from '../../store/slices/behaviorTypesSlice';
import { useBehaviorTypeForm } from '../../hooks/behaviorTypeHooks';
import { BehaviorTypeForm } from './BehaviorTypeForm';
import { BehaviorType } from '../../services/types';
import { useAppDispatch } from '../../store/hooks';
import { Alert } from 'react-bootstrap';
import BaseModal from '../BaseModal';
import { useEffect } from 'react';

interface EditBehaviorTypeModalProps {
    show: boolean;
    behaviorType: BehaviorType | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditBehaviorTypeModal({ show, behaviorType, onCancel, onSuccess }: EditBehaviorTypeModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading, handleInputChange, handleSelectChange,
        handleCheckboxChange, validateForm, resetForm } = useBehaviorTypeForm({ show, isEdit: true, behaviorType });
    
    useEffect(() => {
        if (show && behaviorType && behaviorType.active === undefined) {
            onCancel();
            setFormErrors({ 'general': 'Cannot edit this behavior type' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, behaviorType, onCancel, setFormErrors]);
    
    const handleSubmit = () => {
        if (!validateForm() || !behaviorType || !behaviorType.id) return;
        const behaviorTypeData = {
            name: formData.name,
            pointValue: formData.pointValue,
            active: formData.active,
        };
        dispatch(modifyBehaviorType({ id: behaviorType.id, behaviorTypeData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to update behavior type:', error);
            });
    }
    
    const handleClose = () => {
        resetForm();
        onCancel();
    }
    
    return (
        <BaseModal
            title='Edit Behavior Type'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {formErrors.general && <Alert variant='danger'>{formErrors.general}</Alert>}
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
