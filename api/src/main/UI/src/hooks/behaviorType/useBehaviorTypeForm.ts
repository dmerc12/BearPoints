import { behaviorTypeValidationRules } from '../../utils';
import { BehaviorTypeDTO, Role } from '../../services';
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
    const isAdmin = currentUser?.role === Role.ADMIN;

    const initialData = {
        name: '',
        pointValue: 1,
        active: true
    };

    const form = useForm({
        initialData,
        validationRules: behaviorTypeValidationRules
    });

    useEffect(() => {
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

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
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        error,
        loading,
        isAdmin,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
}
