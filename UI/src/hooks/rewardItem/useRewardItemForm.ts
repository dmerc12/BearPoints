import { rewardItemValidationRules } from '../../utils';
import { RewardItemDTO } from '../../services';
import { useAppSelector } from '../../store';
import { useEffect, useRef } from 'react';
import { useForm } from '../index';

export interface UseRewardItemFormProps {
    show: boolean;
    isEdit?: boolean;
    rewardItem?: RewardItemDTO | null;
}

export const useRewardItemForm = ({ show, isEdit = false, rewardItem }: UseRewardItemFormProps) => {
    const { loading, error } = useAppSelector(state => state.rewardItems);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);

    const initialData = {
        name: '',
        pointCost: 0,
        stock: 0,
    };

    const form = useForm({
        initialData,
        validationRules: rewardItemValidationRules
    });

    useEffect(() => {
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
        }
    }, [show]);

    useEffect(() => {
        if (!show) return;
        if (isEdit && rewardItem && rewardItem.id !== prevIdRef.current) {
            form.setFormData({
                name: rewardItem.name,
                pointCost: rewardItem.pointCost,
                stock: rewardItem.stock,
            });
            prevIdRef.current = rewardItem.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, rewardItem, form]);

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
        resetForm: form.resetForm,
    };
};
