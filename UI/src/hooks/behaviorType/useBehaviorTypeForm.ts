import { behaviorTypeValidationRules } from '../../utils';
import { BehaviorTypeDTO } from '../../services';
import { useAppSelector } from '../../store';
import { useEffect, useRef } from 'react';
import { useForm } from '../index';

export interface UseBehaviorTypeFormProps {
    show: boolean;
    isEdit?: boolean;
    behaviorType?: BehaviorTypeDTO | null;
}

export const useBehaviorTypeForm = ({ show, isEdit = false, behaviorType }: UseBehaviorTypeFormProps) => {
    const { loading, error } = useAppSelector(state => state.behaviorTypes);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);

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
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
        }
    }, [show]);

    useEffect(() => {
        if (!show) return;
        if (isEdit && behaviorType && behaviorType.id !== prevIdRef.current) {
            form.setFormData({
                name: behaviorType.name,
                pointValue: behaviorType.pointValue,
                active: behaviorType.active
            });
            prevIdRef.current = behaviorType.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, behaviorType, form]);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        error,
        loading,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
}
