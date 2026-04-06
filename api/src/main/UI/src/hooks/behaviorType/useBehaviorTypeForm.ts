import { BehaviorTypeDTO, BehaviorTypeFormData } from '../../services';
import { behaviorTypeValidationRules } from '../../utils';
import { useAppSelector } from '../../store';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseBehaviorTypeFormProps {
    show: boolean;
    isEdit?: boolean;
    behaviorType?: BehaviorTypeDTO | null;
}

export const useBehaviorTypeForm = ({ show, isEdit = false, behaviorType }: UseBehaviorTypeFormProps) => {
    const { loading, error } = useAppSelector(state => state.behaviorTypes);
    const currentUser = useAppSelector(state => state.user.data);

    const initialData: BehaviorTypeFormData = {
        name: '',
        pointValue: 1,
        active: true
    };

    const form = useForm({
        initialData,
        validationRules: behaviorTypeValidationRules
    });

    useEffect(() => {
        if (show && isEdit && behaviorType) {
            form.setFormData({
                name: behaviorType.name,
                pointValue: behaviorType.pointValue,
                active: behaviorType.active
            });
        }
    }, [show, isEdit, behaviorType, form]);

    return {
        formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, currentUser, error, loading, handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange, handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm, resetForm: form.resetForm
    };
}
